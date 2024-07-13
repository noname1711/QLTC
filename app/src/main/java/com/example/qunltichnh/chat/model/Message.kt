package com.example.qunltichnh.chat.model

data class Message(
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = 0
)