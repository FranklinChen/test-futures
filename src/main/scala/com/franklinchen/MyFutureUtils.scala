package com.franklinchen

import scala.language.higherKinds
import scala.util._
import scala.concurrent._
import scala.collection.generic.CanBuildFrom
import java.util.concurrent.atomic.AtomicInteger

object MyFutureUtils {
  /**
    Answer to http://stackoverflow.com/questions/16256279/wait-for-several-futures

    This was useful to study: https://github.com/scala/scala/blob/master/src/library/scala/concurrent/Future.scala

    If any success, return it immediately.
    If all futures have completed, with no success, return the final failure.
   */
  def firstOf[T](futurestravonce: TraversableOnce[Future[T]])(implicit executor: ExecutionContext): Future[T] = {
    val futures = futurestravonce.toBuffer
    val remaining = new AtomicInteger(futures.size)

    val p = promise[T]

    futures foreach {
      _ onComplete {
        case s @ Success(_) => {
          p tryComplete s
        }
        case f @ Failure(_) => {
          if (remaining.decrementAndGet == 0) {
            // Arbitrarily return the final failure
            p tryComplete f
          }
        }
      }
    }

    p.future
  }

  /**
    If any failure, return it immediately, else return the final success.

    Dual of firstOf.
   */
  def allSucceed[T](futurestravonce: TraversableOnce[Future[T]])(implicit executor: ExecutionContext): Future[T] = {
    val futures = futurestravonce.toBuffer
    val remaining = new AtomicInteger(futures.size)

    val p = promise[T]

    futures foreach {
      _ onComplete {
        case s @ Success(_) => {
          if (remaining.decrementAndGet == 0) {
            // Arbitrarily return the final success
            p tryComplete s
          }
        }
        case f @ Failure(_) => {
          p tryComplete f
        }
      }
    }

    p.future
  }

  /**
     Someone else's answer on StackOverflow, returning whole successful sequence
  */
  def sequenceOrBailOut[A, M[_] <: TraversableOnce[_]](in: M[Future[A]] with TraversableOnce[Future[A]])(implicit cbf: CanBuildFrom[M[Future[A]], A, M[A]], executor: ExecutionContext): Future[M[A]] = {
    val p = Promise[M[A]]()

    // the first Future to fail completes the promise
    in.foreach(_.onFailure{case i => p.tryFailure(i)})

    // if the whole sequence succeeds (i.e. no failures)
    // then the promise is completed with the aggregated success
    Future.sequence(in).foreach(p trySuccess _)

    p.future
  }

}