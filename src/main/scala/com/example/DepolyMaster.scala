package com.example

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

object DepolyMaster {

  def main(args: Array[String]) {
    val system = ActorSystem("DepolyMaster", ConfigFactory.load("depoly-master"))
    
    val reporter = system.actorOf(DepolyReporter.props)
    
    val king = system.actorOf(PenguinKing(10, reporter), "penguin-king")

    StdIn.readLine()
    Await.result(system.terminate(), Duration.Inf)

    println("end")
  }
}