package com.example.gifty.Data

data class Event(
    val id: Int,
    val name: String,
    var reminder_time: String,
    val description: String,
    var event_date: String
)