package com.example

import akka.actor.ActorSystem

object SupervisorDemo {
  
  def main(args: Array[String]) {
    val system = ActorSystem("SupervisorDemo")
    
    val manager = system.actorOf(PenguinManager.props)
    
    val king = system.actorOf(PenguinKing(10, manager), "penguin-king")
    
    val reporter = system.actorOf(Reporter.props)
    
    Thread.sleep(5000)
    
    (1 to 100) foreach { i =>
      manager.tell(Hit, reporter)
    }
    
    system.awaitTermination()
  }
}