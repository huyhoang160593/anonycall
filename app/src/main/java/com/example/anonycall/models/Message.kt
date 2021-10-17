package com.example.anonycall.models

data class Message(
    val message: String,
    val id:String,
    val time:String
)

data class MessageEvent(val message: String)
