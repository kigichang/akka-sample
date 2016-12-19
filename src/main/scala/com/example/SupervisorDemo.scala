package com.example

import akka.actor.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

object SupervisorDemo {
  
  def main(args: Array[String]) {
    val system = ActorSystem("SupervisorDemo")
    
    val manager = system.actorOf(PenguinManager.props)
    
    val king = system.actorOf(PenguinKing(10, manager), "penguin-king")
    
    val reporter = system.actorOf(Reporter.props)

    StdIn.readLine()
    
    (1 to 100) foreach { i =>
      manager.tell(Hit, reporter)
    }

    Await.result(system.terminate(), Duration.Inf)
  }
}