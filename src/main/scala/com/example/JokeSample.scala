package com.example

import akka.actor.ActorSystem
import akka.actor.ActorRef

object JokeSample {

  def main(args: Array[String]) {
    val system = ActorSystem("JokeSample")

    val reporter = system.actorOf(Reporter.props, "Reporter")

    val penguins = new Array[ActorRef](10)

    for (i <- 0 to 8) {
      penguins(i) = system.actorOf(Penguin(s"Penguin-${i}"))
    }

    penguins(9) = system.actorOf(DongDong.props)

    /* 主程式等一下，要不然上面都是 non-blocking call，會直接結束程式 */
    penguins foreach { _.tell(Interest, reporter) }

    Thread.sleep(10 * 1000)

    system.shutdown()
    println("end")
  }

}