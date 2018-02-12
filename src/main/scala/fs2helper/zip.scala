import cats.data.OptionT
import fs2._
import fs2.io
import cats.effect._
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import scala.concurrent.ExecutionContext

/**
  * Example of zipping, it should provide a blocking pool: Taken from gitter:
  * wedens @wedens 13:19
  * in my zip/unzip implementation, I should provide a blocking pool, right? 
  * it does blocking (I think all of them are blocking) things like 
  * unsafeRunSync/writeOutputStream/toInputStream/readInputStream and some non-blocking 
  * things like concurrently/unboundedQueue

  * Fabio Labella @SystemFw 13:22
  * probably yes 
  *
  **/

@SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
object zip { /*
=======
object zip {

  val chunkSize = 1024

  def zipP[F[_]](chunkSize: Int = chunkSize)(implicit
    F: Effect[F],
    ec: ExecutionContext
  ): Pipe[F, (String, Stream[F, Byte]), Byte] =
    entries => {
      Stream.eval(fs2.async.unboundedQueue[F, Option[Vector[Byte]]]).flatMap { q =>
        def writeEntry(zos: ZipOutputStream): Sink[F, (String, Stream[F, Byte])] =
          _.flatMap {
            case (name, data) =>
              val createEntry = Stream.eval(F.delay {
                zos.putNextEntry(new ZipEntry(name))
              })
              val writeEntry = data.to(
                io.writeOutputStream(
                  F.delay(zos),
                  closeAfterUse = false))
              val closeEntry = Stream.eval(F.delay(zos.closeEntry()))

              createEntry ++ writeEntry ++ closeEntry
          }

        Stream.suspend {
          // `ZipOutputStream` does a lot of single byte writes
          // so doing some buffering drastically improves
          // performance
          @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
          val os = new java.io.OutputStream {
            // synchronously enqueue chunk
            // `None` terminates the queue
            @SuppressWarnings(Array("org.wartremover.warts.Throw"))
            private def enqueueChunkSync(a: Option[Vector[Byte]]) = {
//              q.enqueue1(a).to[IO].unsafeRunSync
            }

            @scala.annotation.tailrec
            private def addChunk(newChunk: Vector[Byte]): Unit = {
              val newChunkSize = newChunk.size
              val bufferedChunkSize = bufferedChunk.size
              val spaceLeftInTheBuffer = newChunkSize - bufferedChunkSize

              if (newChunkSize > spaceLeftInTheBuffer) {
                // Not enough space in the buffer to contain whole new chunk.
                // Recursively slice and enqueue chunk
                // in order to preserve chunk size.

                val fullBuffer = bufferedChunk ++ newChunk.take(spaceLeftInTheBuffer)
                enqueueChunkSync(Some(fullBuffer))
                bufferedChunk = Vector.empty
                addChunk(newChunk.drop(spaceLeftInTheBuffer))
              } else {
                // There is enough space in the buffer for whole new chunk
                bufferedChunk = bufferedChunk ++ newChunk
              }
            }

            @SuppressWarnings(Array("org.wartremover.warts.Var"))
            private var bufferedChunk: Vector[Byte] = Vector.empty

            override def close(): Unit = {
              // flush remaining chunk
              enqueueChunkSync(Some(bufferedChunk))
              bufferedChunk = Vector.empty
              // terminate the queue
              enqueueChunkSync(None)
            }

            override def write(bytes: Array[Byte]): Unit =
              addChunk(Vector(bytes: _*))
            override def write(bytes: Array[Byte], off: Int, len: Int): Unit =
              addChunk(Chunk.bytes(bytes, off, len).toVector)
            override def write(b: Int): Unit =
              addChunk(Vector(b.toByte))
          }

          val write = Stream.bracket(F.delay(new ZipOutputStream(os)))(
            zos => entries.to(writeEntry(zos)),
            zos => F.delay(zos.close()))

          import cats.implicits._
          val read = q
            .dequeue
            .unNoneTerminate // `None` in the stream terminates it
            .flatMap(Stream.emits(_))

          read concurrently write
        }
      }
    }

  def zip[F[_]](entries: Stream[F, (String, Stream[F, Byte])], chunkSize: Int = chunkSize)(implicit F: Effect[F], ec: ExecutionContext): Stream[F, Byte] =
    entries.through(zipP(chunkSize))

  def unzipP[F[_]](chunkSize: Int = chunkSize)(
    implicit F: Effect[F],
             ec: ExecutionContext
  ): Pipe[F, Byte, (String, Stream[F, Byte])] = {
    def entry(zis: ZipInputStream): OptionT[F, (String, Stream[F, Byte])] =
      OptionT(F.delay(Option(zis.getNextEntry()))).map { ze =>
        (ze.getName,
          io.readInputStream[F](
            F.delay(zis),
            chunkSize,
            closeAfterUse = false))
      }

    def unzipEntries(zis: ZipInputStream): Stream[F, (String, Stream[F, Byte])] =
      Stream.unfoldEval(zis) { zis0 =>
        entry(zis0).map((_, zis0)).value
      }

    _.through(io.toInputStream).flatMap { is =>
      Stream.bracket(F.delay(new ZipInputStream(is)))(
        unzipEntries,
        zis => F.delay(zis.close()))
    }
  }

  def unzip[F[_]](zipped: Stream[F, Byte], chunkSize: Int = chunkSize)(
    implicit F: Effect[F],
             ec: ExecutionContext
  ): Stream[F, (String, Stream[F, Byte])] =
    zipped.through(unzipP(chunkSize))
    */
}
