import $ivy.`scalavision::fs2helper:0.1-SNAPSHOT`
//import $ivy.`scalaz8-effect::scalaz8-effect:0.1-SNAPSHOT`

import fs2._
import fs2helper.Fs2Helper._
import cats.effect.IO
//import scalaz.effect.IO
import scala.concurrent.duration._
//import scala.concurrent.ExecutionContext._
//import scala.concurrent.ExecutionContext.Implicits.global

type Message = String

val poll: IO[Message] = IO { "latest message" }
/*
val latestMessage: Stream[IO, Option[Message]] =  {
  // Scheduler with two threads
  //Scheduler[IO](2)
//  scheduler.flatMap { sch => 
    // retryPoll as long as it returns an error (does IO have an error ..? )
    // use an exponential backoff between each try
    val retryPoll: Stream[IO, Message] = 
      scheduler.retry(poll, 1.second, _ * 2, maxRetries = Int.MaxValue)
    
    val repeatPoll: Stream[IO, Message] = 
      // retryPoll returns an IO of Message, the sleep returns an IO of Nothing
      // the sleep is just there as an effect to make the poll be repeated. 
      (retryPoll ++ scheduler.sleep_[IO](5.seconds)).repeat

    async.holdOption(repeatPoll).flatMap(_.continuous)  
}


latestMessage.run.unsafeRunAsync(println)


val s = Scheduler[IO](corePoolSize = 1).flatMap { scheduler =>
  scheduler.awakeEvery[IO](1.second).flatMap(_ => Stream(1, 2, 3))
}

s.runLog.unsafeRunSync
*/

def log[A](prefix: String): Pipe[IO, A, A] = _.evalMap { m =>
  IO { println(s"$prefix > $m" ); m }
}

def randomDelays[A](max: FiniteDuration): Pipe[IO, A, A] = _.flatMap { m =>
  val randomDelay = scala.util.Random.nextInt(max.toMillis.toInt) / 1000.0
  println(s"delay: $randomDelay")
  scheduler.delay(Stream.eval(IO(m)), randomDelay.seconds)
}

println("starting up the stream")

val a = Stream.range(1,20).covary[IO]
  .through(randomDelays(2.seconds))
  .through(log("A"))


val b = Stream.range(1,20).covary[IO]
  .through(randomDelays(2.seconds))
  .through(log("B"))

val c = Stream.range(1,20).covary[IO]
  .through(randomDelays(2.seconds))
  .through(log("C"))

// A will always wait for B ... 
val sInterLeave: Stream[IO, Int] = (a interleave b).through(log("interleaved")) //Timed(10.seconds)
//sInterleave.run.unsafeRunAsync(println)

// We pull asynchronously from both of our input streams
val sMerge: Stream[IO, Int] = (a merge b).through(log("merged"))
//sMerge.run.unsafeRunAsync(println)

// Remembers which size it came from, it also is able to handle different types on 
// left and right side
val sEither: Stream[IO, Int] = (a merge b ).through(log("either"))
//sEither.run.unsafeRunAsync(println)

// Creating a stream of streams
// The inner stream may be a stream from the file we want to read, while the
// outer stream may be all possible files within a directory over time
// Or the inner stream represents a tcp connection from a client to our server,
// and the outer stream represents every individual client that connects to our server
val sStreams: Stream[IO, Stream[IO, Int]] = Stream(a, b, c)

// We will sort of `flatten` the stream into one, using three streams in parallell
// with join (changed from 0.9 and 0.10.0-M6. This works like merge
val parallellStream: Stream[IO, Int] = sStreams.join(3)
//parallellStream.run.unsafeRunAsync(println)

val manyStreams = Stream.range(0,10).map { id =>
  Stream.range(1, 10).covary[IO].through(randomDelays(2.seconds)).through(log(('A' + id).toChar.toString))
}
//manyStreams.join(3).run.unsafeRunAsync(println)

val x: IO[async.mutable.Signal[IO, Int]] = async.signalOf[IO, Int](0)

val xIO: IO[Int] = x.flatMap { x1: async.mutable.Signal[IO, Int] => x1.get }

// We can run this effect to get the value
//pprint.pprintln(xIO.unsafeRunSync)

// We can convert this signal into a single element stream:
val sx: Stream[IO, async.mutable.Signal[IO, Int]] = Stream.eval(x)

// We can create a stream that stream that listenes to and propagates all changes
// to the stream
val sRx: Stream[IO, Int] = Stream.eval(x).flatMap { x1 => x1.discrete }

// We can collect the first signal, without blocking our repl, using fromFuture
// This will actually print our signal
//sRx.through(log("signal")).run.unsafeToFuture

// cheating ... We unlock our signal with running it
val sxValues: async.mutable.Signal[IO, Int] = x.unsafeRunSync//unsafeRunAsync(println)

sxValues.discrete.through(log("second")).run.unsafeRunAsync(println)

// lets change the signal, and you will see that the 
// sxValues above outputs the new value
val sxTrigger = sxValues.set(2)
sxTrigger.unsafeRunSync
// If we write to the signal again, it will output once more
//Thread.sleep(1000)
sxTrigger.unsafeRunSync
//sxValues.set(1).unsafeRunSync

/** This does not work so well
val signalStuff: Stream[IO, Stream[IO, Int]] = for {
  s <- Stream.range(1, 10).covary[IO].through(randomDelays(2.seconds))
  _ <- Stream.eval(x.flatMap { x1 => IO { x1.set(s + 1) } })
  r <- Stream.eval(x.flatMap { x1 => IO { x1.discrete } })
//  _ <- Stream.eval(x).flatMap { x1 => x1.set(s) }
//  r <- Stream.eval(x).flatMap { x1 => x1.get }
} yield r

signalStuff.covary[IO].join(2).through(log("signal")).run.unsafeRunAsync(println)
*/
