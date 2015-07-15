package com.example

import akka.actor.Actor
import akka.actor.Props
import akka.actor.Identify
import scala.concurrent.duration.DurationInt
import akka.actor.ActorIdentity
import scala.concurrent.duration.Duration
import akka.actor.ActorRef

/**
 * 企鵝
 */
class Penguin(val name: String) extends Actor {
  def receive: Actor.Receive = {
    case Interest =>
      println(s"$name got Interest")
      sender() ! Three(name, "吃飯", "睡覺", "打東東")

    case Why =>

    case _ =>
  }

  override def preStart() = {
    println(s"$name start at ${self.path}")
  }
}

object Penguin {
  def apply(name: String): Props = Props(classOf[Penguin], name)
}

/**
 * 叫東東的企鵝
 */
class DongDong extends Penguin("東東") {

  override def receive = {
    case Interest =>
      println(s"$name got Interest")
      sender() ! Two(name, "吃飯", "睡覺")

    case Why =>
      println(s"$name got Why")
      sender() ! Because(name, s"我就是【${name}】")

    case _ =>
  }
}

object DongDong {
  val props = Props[DongDong]
}

/**
 * 記者
 */
class Reporter extends Actor {
  def receive: Actor.Receive = {

    /* 有三個興趣的回覆 */
    case Three(name, a, b, c) =>
      println(s"${name}: ${a}, ${b}, ${c}")

    /* 只有二個興趣的回覆，反問 why */
    case Two(name, a, b) =>
      println(s"${name}: ${a}, ${b}")
      sender ! Why

    /* 接到 why 的回覆 */
    case Because(name, msg) =>
      println(s"${name}: ${msg}")

    case _ =>
  }
}

object Reporter {
  val props = Props[Reporter]
}

/**
 * Lookup 版的記者
 */
class LookupReporter(penguins: Array[String]) extends Reporter {

  var count = 0

  def sendIdentifyRequest() {
    penguins foreach { path => println(path); context.actorSelection(path) ! Identify(path) }

    context.setReceiveTimeout(5 seconds)
  }

  sendIdentifyRequest

  def myReceive: Actor.Receive = {
    case ActorIdentity(path, Some(actor)) =>
      count += 1

      if (count == penguins.length) {
        context.setReceiveTimeout(Duration.Undefined)
      }

      println(s"${path} found")

      actor ! Interest

    case ActorIdentity(path, None) =>
      println(s"${path} not found")
  }

  override def receive: Actor.Receive = myReceive orElse super.receive

  override def preStart() = {
    println("Lookup Reporter start")
  }
}

object LookupReporter {

  def apply(penguins: Array[String]) = Props(classOf[LookupReporter], penguins)
}

/**
 * 企鵝王
 */
class PenguinKing(count: Int, reporter: ActorRef) extends Actor {

  def depoly = {
    for (i <- 0 until count - 1) {
      val penguin = context.actorOf(Penguin(s"penguin-$i"))

      penguin ! Identify(penguin.path.toString)

    }

    val dongdong = context.actorOf(DongDong.props)
    
    dongdong ! Identify(dongdong.path.toString)
  }

  depoly
  
  def receive: Actor.Receive = {
    case ActorIdentity(path, Some(actor)) =>
      println(s"$path found")
      reporter ! PenguinReady(path.toString)
      
    case ActorIdentity(path, None) =>
      println(s"$path not found")
  }
}

object PenguinKing {
  def apply(count: Int, reporter: ActorRef) = Props(classOf[PenguinKing], count, reporter)
}

/**
 * Depolyment 版記者
 */
class DepolyReporter extends Reporter {

  def myReceive: Actor.Receive = {
    case PenguinReady(path) =>
      val actor = context.actorSelection(path)
      actor ! Interest
  }

  override def receive = myReceive orElse super.receive
}

object DepolyReporter {
  val props = Props[DepolyReporter]
}