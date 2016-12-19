name := """akka-sample"""

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.14",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.14" % "test",
  "com.typesafe.akka" %% "akka-http" % "10.0.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")