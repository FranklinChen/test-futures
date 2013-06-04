package com.franklinchen

import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

// Just messing around.
object FuturesRun {
  def makeFuture(s: String, i: Int) = future {
    Thread.sleep(1000)
    println(s + " " + i)
  }

  def makeFutures(s: String) =
    1 to 10 map { makeFuture(s, _) }

  def mine() {
    println("Start mine:")

    val f = MyFutureUtils.allSucceed(makeFutures("my"))
    f onSuccess {
      case s => println("GOT MINE")
    }

    Await.result(f, Duration.Inf)
  }

  def theirs() {
    println("Start theirs:")
    val f = MyFutureUtils.sequenceOrBailOut(makeFutures("their"))
    f onSuccess {
      case s => println("GOT THEIRS")
    }

    Await.result(f, Duration.Inf)
 }

  def mySequence() {
    val f = Future.sequence(makeFutures("my sequence"))
    f onSuccess {
      case s => println("GOT MY SEQUENCE")
    }

    Await.result(f, Duration.Inf)
  }

  def main(args: Array[String]) {
    mine()
    mySequence()
    theirs()
  }

}