package com.example

sealed trait Question
case object Interest extends Question
case object Why extends Question
case object QueryCount extends Question
case object KillOne extends Question
case object Hit extends Question