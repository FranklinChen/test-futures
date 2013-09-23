package com.franklinchen

import org.specs2._
import org.specs2.specification._
import org.specs2.time.NoTimeConversions

import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class FuturesSpec extends Specification with NoTimeConversions { 
  sequential

  def is = s2"""
    Test futures (warning: not deterministic because of scheduling)

      MyFutureUtils.firstOf

        first one wins if all successful $e1
        failures ignored if one succeeds $e2
        return last failure if all fail  $e3

      Future.sequence

        wait for all to succeed             $e4
        return last failure if there is one $e5

      Timeout

        infinite loop $e6
    """

  def e1 = {
    val f = MyFutureUtils.firstOf(Seq(
      future { Thread.sleep(2000); 3 },
      future { Thread.sleep(1000); 2 },
      future { Thread.sleep(1); 1 }
    ))

    val v = Await.result(f, Duration.Inf)
    v must_== 1
  }

  def e2 = {
    val f = MyFutureUtils.firstOf(Seq(
      future { 1/0 },
      future { throw new IllegalArgumentException },
      future { Thread.sleep(1000); 1 }
    ))

    val v = Await.result(f, Duration.Inf)
    v must_== 1
  }

  def e3 = {
    val f = MyFutureUtils.firstOf(Seq(
      future { Thread.sleep(1500); throw new IllegalArgumentException("last"); 1 },
      future { Thread.sleep(100); throw new IllegalArgumentException("late") },
      future { Thread.sleep(10); throw new IllegalArgumentException("early") }
    ))

    Await.result(f, Duration.Inf) must
    throwA[IllegalArgumentException](message = "last")
  }

  def e4 = {
    val f = Future.sequence(Set(
      future { Thread.sleep(300); 3 },
      future { Thread.sleep(200); 2 },
      future { Thread.sleep(100); 1 }
    ))

    val v = Await.result(f, Duration.Inf)
    v must_== Set(3, 2, 1)
  }

  def e5 = {
    val f = Future.sequence(Set(
      future { 5 },
      future { Thread.sleep(1000); throw new IllegalArgumentException("late") },
      future { throw new IllegalArgumentException("early") }
    ))

    Await.result(f, Duration.Inf) must
    throwA[IllegalArgumentException](message = "late")
  }

  def e6 = {
    val f = future {
      while (true) {
        // nothing
      }
    }

    import scala.concurrent.duration._
    Await.result(f, 5.seconds) must
    throwA[TimeoutException]
  }
}
