package com.example

import com.typesafe.config.ConfigFactory
import akka.actor.ActorRef
import akka.actor.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

object LookupServer {

  def main(args: Array[String]) {

    val system = ActorSystem("LookupServer", ConfigFactory.load("lookup-server"))

    val penguins = new Array[ActorRef](10)

    for (i <- 0 to 8) {
      penguins(i) = system.actorOf(Penguin.props(s"Penguin-$i"), s"penguin-$i")
      println(penguins(i).path)
    }

    penguins(9) = system.actorOf(DongDong.props, "dongdong")
    println(penguins(9).path)

    StdIn.readLine()
    Await.result(system.terminate(), Duration.Inf)
    println("end")
  }
}