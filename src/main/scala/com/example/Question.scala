package com.example

sealed trait Question
object Interest extends Serializable with Question
object Why extends Serializable with Question
object QueryCount extends Serializable with Question
object KillOne extends Serializable with Question
object Hit extends Serializable with Question