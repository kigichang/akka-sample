package com.example

sealed trait Question
object Interest extends Serializable with Question
object Why extends Serializable with Question
