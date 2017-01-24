package com.example

import akka.actor.{Actor, ActorSystem, OneForOneStrategy, Props, Terminated}
import akka.actor.SupervisorStrategy.{Escalate, Restart, Stop}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn
import scala.util.control.NonFatal
import scala.concurrent.duration._

/**
  * Created by kigi on 20/01/2017.
  */
object TestRestartTerminated {


  case class Counter(n: Int)

  class Child extends Actor {
    var count = 0

    override def receive = {
      case Counter(n) =>
        count += 1
        println(s"current count: $count")

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

    val child = context.actorOf(Props[Child], "children")
    context watch child

    (1 to 10) foreach { i =>
      child ! Counter(i)
    }
    println("sent")

    override def receive = {
      case Terminated(child) =>
        println(s"${child.path} terminated")
      case _ =>
    }

    override def preStart(): Unit = {
      println("parent pre start")
      super.preStart()
    }

    override def postStop(): Unit = {
      println("parent post stop")
      super.postStop()
    }

  }

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("TestHook")

    system.actorOf(Props[Test])

    StdIn.readLine
    Await.result(system.terminate(), Duration.Inf)
  }


}
