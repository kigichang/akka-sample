package com.example

sealed trait Answer
case class Three(name: String, a: String, b: String, c: String) extends Answer
case class Two(name: String, a: String, b: String) extends Answer
case class Because(name: String, msg: String) extends Answer

case class PenguinReady(path: String)