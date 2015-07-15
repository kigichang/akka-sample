package com.example

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object DepolynNode {

  def main(args: Array[String]) {
    val system = ActorSystem("DepolyNode", ConfigFactory.load("depoly-node"))
  }
}