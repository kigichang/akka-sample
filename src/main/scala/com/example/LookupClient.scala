package com.example

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

object LookupClient {

  def main(args: Array[String]) {
    val system = ActorSystem("LookupClient", ConfigFactory.load("lookup-client"))

    val remotePath = "akka.tcp://LookupServer@127.0.0.1:2552/user/"

    val penguins = new Array[String](10)

    for (i <- 0 to 8) {
      penguins(i) = s"${remotePath}penguin-${i}"
    }

    penguins(9) = s"${remotePath}dongdong"

    val reporter = system.actorOf(LookupReporter(penguins), "reporter")
    println(reporter.path)

    /* 主程式等一下，要不然上面都是 non-blocking call，會直接結束程式 */

    StdIn.readLine()
    Await.result(system.terminate(), Duration.Inf)

    println("end")
  }
}