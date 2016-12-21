package com.example

import scala.concurrent.duration.{Duration, DurationInt}
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Inbox
import akka.actor.Props
import akka.actor.actorRef2Scala

import scala.concurrent.Await

case class ToWhom(name: String)
case class Response(msg: String)

class MyActor extends Actor {
  def receive: Actor.Receive = {
    case ToWhom(name) => sender() ! Response(s"Hi $name. Hello World!!!")
    case _ =>
  }
}

object MyActor {
  val props = Props[MyActor]
}

object HelloWorld {

  def main(args: Array[String]) {
    /* 啟動 Akka micro-system */
    val system = ActorSystem("HelloWorld")

    /* 產生一個 Actor。 */
    val actor = system.actorOf(MyActor.props)

    /* 看一下 Actor 註冊的 path。如果要重覆使用已產生的 Actor，就需要知道 Actor 的 path。
   * 這個在遠端執行很重要 
   * */
    val path = actor.path
    println(path)

    /* demo 使用已產生的 actor */
    val actor2 = system.actorSelection(path)

    /* 產生一個收信箱. 等一下用此收件箱的名義，對 MyActor 送訊息 */
    val inbox = Inbox.create(system)
    // 以下是多種傳 message 方式，且都是 non-blocking (Fire-and-Forgot)。
    // 方法1
    //inbox.send(actor, ToWhom("小明"))
    // 方法2
    actor.!(ToWhom("小華"))(inbox.getRef)
    // 方法3
    //actor.tell(ToWhom("東東"), inbox.getRef)

    // Blocking call，等待回覆訊息，最多等 5 sec。不等的話，程式會往下執行，就結束了。
    val Response(msg) = inbox.receive(5 seconds)
    println(msg)

    /* 記得要 shutdown, 要不然程式不會結束 */
    Await.result(system.terminate(), Duration.Inf)
    println("end")
  }

}