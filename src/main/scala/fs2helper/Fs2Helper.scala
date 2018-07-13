package fs2helper

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.{Executors, ThreadFactory, TimeUnit}
import java.util.concurrent.atomic.AtomicInteger
import java.nio.channels.AsynchronousChannelGroup

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import java.util.concurrent.TimeUnit

import fs2._
import cats.effect.IO
import scala.concurrent.duration._

object Fs2Helper {

  implicit val ec : ExecutionContext = 
      ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(
          8, threadFactory("ExecutionContext", daemon = true
      )))

  //val sch: Stream[IO, Scheduler] = Stream.emit(Scheduler.default)

  /*
  implicit val sch : Scheduler = 
    Scheduler.fromScheduledExecutorService(
      Executors.newScheduledThreadPool(
        4, threadFactory("Scheduler", daemon = true
    )))*/

  implicit val acg: AsynchronousChannelGroup = 
    AsynchronousChannelGroup.withThreadPool(
      Executors.newCachedThreadPool(
        threadFactory("AsynchChannelGroup", daemon = true
    )))


  
  def threadFactory(name: String, daemon: Boolean, exitJvmOnFatalError: Boolean = true): ThreadFactory = {
    new ThreadFactory {
      val idx = new AtomicInteger(0)
      val defaultFactory = Executors.defaultThreadFactory()
      def newThread(r: Runnable): Thread = {
        val thread = defaultFactory.newThread(r)
        thread.setName(s"${name}_${idx.incrementAndGet()}")
        thread.setDaemon(daemon)
        thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler {
          def uncaughtException(t: Thread, e: Throwable): Unit = {
            ExecutionContext.defaultReporter(e)
            if (exitJvmOnFatalError) {
              e match {
                case NonFatal(_) => ()
                case fatal => System.exit(-1)
              }
            }
          }
        })
        thread
      }
    }
  }

  def log[A](prefix: String): Pipe[IO, A, A] = _.evalMap { m =>
    IO { println(s"$prefix > $m" ); m }
  }

  //TODO: test this, probably way off ...
  def randomDelays[A](max: FiniteDuration): Pipe[IO, A, A] = _.flatMap { m =>
    val randomDelay = scala.util.Random.nextInt(max.toMillis.toInt) / 1000.0

    println(s"delay: $randomDelay")
    //Stream.eval(IO.delay(randomDelay.seconds).map { a => m } )
    Stream.eval(IO { Thread.sleep(scala.util.Random.nextLong) }.map { a => m } )

  }

  def shutdown(): Unit = {
    println("shutting down!")
    acg.shutdownNow()
    println("awaiting termination ....")
    acg.awaitTermination(10, TimeUnit.SECONDS)
    println("has shutdown: " + acg.isShutdown())
    println("has terminated: " + acg.isTerminated())
  }

}
