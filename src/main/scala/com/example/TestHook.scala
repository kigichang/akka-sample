package com.example

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorSystem, OneForOneStrategy, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import scala.io.StdIn
import scala.util.control.NonFatal

/**
  * Created by kigi on 22/12/2016.
  */
object TestHook {

  case class Counter(n: Int)


  class Child extends Actor {
    override def receive = {
      case Counter(n) =>
        println(s"$n")

        if (n == 2) {
          Thread.sleep(1000)
          throw new NullPointerException(s"$n")
        }
    }


    override def preStart(): Unit = {
      println("child pre start")
    }

    override def postStop(): Unit = {
      println("child post stop")
    }

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      println(s"child pre restart: ${reason.toString}, ${message.toString}")
      super.preRestart(reason, message)
    }

    override def postRestart(reason: Throwable): Unit = {
      println(s"child post restart: ${reason.toString}")
      super.postRestart(reason)
    }
  }

  class Test extends Actor {
    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case NonFatal(ex) =>
        println(s"catch ex: ${ex.toString}")
        Restart
      case _ =>
        Escalate
    }

    val child = context.actorOf(Props[Child])

    (1 to 100) foreach { i =>
      child ! Counter(i)
    }
    println("sent")

    override def receive = {
      case _ =>
    }
  }

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("TestHook")

    system.actorOf(Props[Test])

    StdIn.readLine
    Await.result(system.terminate(), Duration.Inf)
  }


}
