package com.example.gifty.Repositories

import com.example.gifty.DataSources.Events.EventDataSourceImpl
import com.example.gifty.Data.Event
import com.example.gifty.DataSources.Events.EventDataSource
import com.google.gson.JsonArray

class EventRepository(private val eventDataSource: EventDataSource) {
    suspend fun createEvent(userId: Int, name: String, reminderTime: String, description: String, eventDate: String): Boolean{
        return eventDataSource.createEvent(userId, name,reminderTime, description, eventDate)
    }
    suspend fun getEventsByUserId(userId: Int): JsonArray{
        return eventDataSource.getEventsByUserId(userId)
    }
    suspend fun deleteEvent(eventId: Int): Boolean{
        return eventDataSource.deleteEvent(eventId)
    }
    suspend fun updateEvent(eventId: Int, name: String, reminder_time: String, description: String): Boolean{
        return eventDataSource.updateEvent(eventId, name, reminder_time, description)
    }
    suspend fun getEventById(eventId: Int): Event? {
        return eventDataSource.getEventById(eventId)
    }
}