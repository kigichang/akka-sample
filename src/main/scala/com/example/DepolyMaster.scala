package com.example

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem

object DepolyMaster {

  def main(args: Array[String]) {
    val system = ActorSystem("DepolyMaster", ConfigFactory.load("depoly-master"))
    
    val reporter = system.actorOf(DepolyReporter.props)
    
    val king = system.actorOf(PenguinKing(10, reporter), "penguin-king")
    
    
    Thread.sleep(10 * 1000)
    
    system.shutdown()
    println("end")
  }
}