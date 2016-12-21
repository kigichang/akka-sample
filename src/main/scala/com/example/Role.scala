package com.example

import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt

import akka.actor.Actor
import akka.actor.ActorIdentity
import akka.actor.ActorRef
import akka.actor.ActorSelection.toScala
import akka.actor.Identify
import akka.actor.OneForOneStrategy
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.SupervisorStrategy.Escalate
import akka.actor.SupervisorStrategy.Restart
import akka.actor.SupervisorStrategy.Resume
import akka.actor.SupervisorStrategy.Stop
import akka.actor.Terminated
import akka.actor.actorRef2Scala
import akka.routing.RoundRobinRoutingLogic
import akka.routing.Router

class DontBotherMeException extends Exception

class ExplodeException extends Exception

class IamGodException extends Exception

/**
 * 企鵝
 */
class Penguin(val name: String) extends Actor {
  var count = 0
  var hits = 0

  def receive: Actor.Receive = {
    case Interest =>
      count += 1

      println(s"$name got Interest")
      sender() ! Three(name, "吃飯", "睡覺", "打東東")

    case Why =>

    case QueryCount =>
      sender ! Counter(name, count)

    case Hit =>
      hits += 1
      println(s"$name got Hit")

    case _ =>
  }

  override def preStart() = {
    println(s"$name start at ${self.path}")
  }
}

object Penguin {
  //def apply(name: String): Props = Props(classOf[Penguin], name)
  def props(name: String): Props = Props(classOf[Penguin], name)
}

/**
 * 叫東東的企鵝
 */
class DongDong extends Penguin("東東") {

  def myReceive: Actor.Receive = {
    case Interest =>
      count += 1
      println(s"$name got Interest")
      sender() ! Two(name, "吃飯", "睡覺")

    case Why =>
      println(s"$name got Why")
      sender() ! Because(name, s"我就是【${name}】")

    case Hit =>
      println(s"$name got Hit")
      hits += 1

      if (hits == 4)
        throw new DontBotherMeException
      else if (hits == 6)
        throw new ExplodeException
      else if (hits > 6)
        throw new IamGodException
      else
        sender ! DontHitMe(name, hits)
  }

  override def receive = myReceive orElse super.receive

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

    case Counter(name, count) =>
      println(s"$name: $count")

    case DontHitMe(name, hits) =>
      println(s"$name hit $hits")
    case _ =>
  }
  
  override def preStart() {
    println("Reporter start")
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

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
    //AllForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
    case _: DontBotherMeException => Resume
    case _: ExplodeException => Restart
    case _: IamGodException => Stop
    case _: Exception => Escalate
  }

  def depoly = {
    for (i <- 0 until count - 1) {
      val penguin = context.actorOf(Penguin.props(s"penguin-$i"))

      penguin ! Identify(penguin.path.toString)

    }

    val dongdong = context.actorOf(DongDong.props)

    dongdong ! Identify(dongdong.path.toString)
  }

  depoly

  def receive: Actor.Receive = {
    case ActorIdentity(path, Some(actor)) =>
      println(s"$path found")
      reporter ! PenguinReady(actor)

    case ActorIdentity(path, None) =>
      println(s"$path not found")
  }
  
  override def preStart() {
    println("PenguinKing start")
  }
}

object PenguinKing {
  def apply(count: Int, reporter: ActorRef) = Props(classOf[PenguinKing], count, reporter)
}

/**
 * 企鵝總管
 */
class PenguinManager extends Actor {
  var router = Router(RoundRobinRoutingLogic())

  def receive: Actor.Receive = {
    case PenguinReady(actor) =>
      context watch actor
      router = router.addRoutee(actor)
      println("manager watch " + actor.path)

    case Terminated(actor) =>
      println("manager remove " + actor.path)
      router = router.removeRoutee(actor)

    case Interest =>
      router.route(Interest, sender)

    case QueryCount =>
      router.routees foreach { actor =>
        actor.send(QueryCount, sender)
      }

    case Hit =>
      router.route(Hit, sender)

    case KillOne =>
      router.routees(0).send(PoisonPill, self)

    case _ =>
  }
  
  override def preStart() {
    println("PenguinManager start")
  }
}

object PenguinManager {
  val props = Props[PenguinManager]
}

/**
 * Depolyment 版記者
 */
class DepolyReporter extends Reporter {

  def myReceive: Actor.Receive = {
    case PenguinReady(actor) =>
      actor ! Interest
  }

  override def receive = myReceive orElse super.receive
  
  override def preStart() {
    println("DepolyReporter start")
  }
}

object DepolyReporter {
  val props = Props[DepolyReporter]
}