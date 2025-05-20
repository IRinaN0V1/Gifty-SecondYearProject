package com.example.gifty.DataSources.Events

import com.example.gifty.Data.Event
import com.google.gson.JsonArray

interface EventDataSource {
    suspend fun createEvent(userId: Int, name: String, reminderTime: String, description: String, eventDate: String): Boolean
    suspend fun getEventsByUserId(userId: Int): JsonArray
    suspend fun deleteEvent(eventId: Int): Boolean
    suspend fun updateEvent(eventId: Int, name: String, reminder_time: String, description: String): Boolean
    suspend fun getEventById(eventId: Int): Event?
}