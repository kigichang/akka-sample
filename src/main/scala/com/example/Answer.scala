package com.example

import akka.actor.ActorRef

sealed trait Answer

case class Three(name: String, a: String, b: String, c: String) extends Answer
case class Two(name: String, a: String, b: String) extends Answer
case class Because(name: String, msg: String) extends Answer
case class Counter(name: String, count: Int) extends Answer
case class DontHitMe(name: String, hits: Int) extends Answer
case class PenguinReady(actor: ActorRef)
