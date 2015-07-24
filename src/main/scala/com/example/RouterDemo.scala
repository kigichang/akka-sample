package com.example

import akka.actor.ActorSystem

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
    
    Thread.sleep(5000)
    
    
    manager.tell(QueryCount, reporter)
    
    Thread.sleep(5000)
    
    manager.tell(KillOne, reporter)
    
    system.awaitTermination()
  }
}