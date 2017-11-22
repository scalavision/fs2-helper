package fs2helper

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.{Executors, ThreadFactory}
import java.util.concurrent.atomic.AtomicInteger
import java.nio.channels.AsynchronousChannelGroup

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

import fs2._

object Fs2Helper {

  implicit val EC : ExecutionContext = 
      ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(
          8, mkThreadFactory("fs2-http-spec-ec", daemon = true
      )))

  implicit val Sch : Scheduler = 
    Scheduler.fromScheduledExecutorService(
      Executors.newScheduledThreadPool(
        4, mkThreadFactory("fs2-http-spec-scheduler", daemon = true
    )))

  implicit val AG: AsynchronousChannelGroup = 
    AsynchronousChannelGroup.withThreadPool(
      Executors.newCachedThreadPool(
        mkThreadFactory("fs2-http-spec-AG", daemon = true
    )))


  /** helper to create named daemon thread factories **/
  def mkThreadFactory(name: String, daemon: Boolean, exitJvmOnFatalError: Boolean = true): ThreadFactory = {
    new ThreadFactory {
      val idx = new AtomicInteger(0)
      val defaultFactory = Executors.defaultThreadFactory()
      def newThread(r: Runnable): Thread = {
        val t = defaultFactory.newThread(r)
        t.setName(s"$name-${idx.incrementAndGet()}")
        t.setDaemon(daemon)
        t.setUncaughtExceptionHandler(new UncaughtExceptionHandler {
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
        t
      }
    }
  }


}
