package com.example

import akka.actor.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

object RouterDemo {

  def main(args: Array[String]) {
    val system = ActorSystem("RouterDemo")
    
    val manager = system.actorOf(PenguinManager.props)
    
    val king = system.actorOf(PenguinKing(10, manager), "penguin-king")
    
    val reporter = system.actorOf(Reporter.props)
    
    Thread.sleep(5000)
    
    (1 to 100) foreach { i =>
      manager.tell(Interest, reporter)
    }

    StdIn.readLine()

    manager.tell(QueryCount, reporter)

    StdIn.readLine()
    
    manager.tell(KillOne, reporter)

    StdIn.readLine()

    Await.result(system.terminate(), Duration.Inf)
  }
}