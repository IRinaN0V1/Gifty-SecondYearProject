package com.example.gifty.DataSources.Events

import com.example.gifty.Api
import com.example.gifty.Data.Event
import com.google.gson.JsonArray

class EventDataSourceImpl(private val api: Api) : EventDataSource {
    override suspend fun createEvent(userId: Int, name: String, reminderTime: String, description: String, eventDate: String): Boolean{
        return try {
            val response = api.createEvent(userId, name,reminderTime, description, eventDate)
            val jsonResponse = response.body()
            jsonResponse?.get("error")?.asBoolean == false
        } catch (e: Exception) {
            false
        }
    }
    override suspend fun getEventsByUserId(userId: Int): JsonArray{
        return try {
            val response = api.getEventsByUserId(userId)
            val jsonResponse = response.body()
            if (jsonResponse != null && !jsonResponse.get("error").asBoolean) {
                val eventsArray = jsonResponse.getAsJsonArray("events")
                return eventsArray
            } else {
                JsonArray()
            }
        } catch (e: Exception) {
            JsonArray()
        }
    }
    override suspend fun deleteEvent(eventId: Int): Boolean{
        return try {
            val response = api.deleteEvent(eventId)
            val jsonResponse = response.body()
            if (jsonResponse != null) {
                jsonResponse.get("error")?.asBoolean == false
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateEvent(eventId: Int, name: String, reminder_time: String, description: String): Boolean{
        return try {
            val response = api.updateEvent(eventId, name, reminder_time, description)
            val jsonResponse = response.body()
            if (jsonResponse != null) {
                jsonResponse.get("error")?.asBoolean == false
            }else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    override suspend fun getEventById(eventId: Int): Event? {
        return try {
            val response= api.getEventById(eventId)
            val jsonResponse = response.body()
            if (jsonResponse != null) {
                if (!jsonResponse.get("error").asBoolean){
                    jsonResponse.let {
                        val eventJson = it.getAsJsonObject("event")
                        Event(eventJson.get("id").asInt, eventJson.get("name").asString, eventJson.get("reminder_time").asString, eventJson.get("description").asString, eventJson.get("event_date").asString)
                    }
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}