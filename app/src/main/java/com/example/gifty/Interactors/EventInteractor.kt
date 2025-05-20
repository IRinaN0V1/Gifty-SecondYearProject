package com.example.gifty.Interactors

import com.example.gifty.Data.Event
import com.example.gifty.Repositories.EventRepository
import com.google.gson.JsonArray
import java.text.SimpleDateFormat
import java.util.Locale

class EventInteractor(private val eventsRepository: EventRepository) {

    suspend fun createEvent(userId: Int, name: String, reminderTime: String, description: String, eventDate: String): Boolean{
        val convertReminderTime = reverseConvertReminderTime(reminderTime)
        val convertEventDate = reverseConvertEventDate(eventDate)
        return eventsRepository.createEvent(userId, name, convertReminderTime, description, convertEventDate)
    }
    suspend fun getEventsByUserId(userId: Int): List<Event> {
        return  jsonConverter(eventsRepository.getEventsByUserId(userId))
    }
    suspend fun deleteEvent(eventId: Int): Boolean{
        return eventsRepository.deleteEvent(eventId)
    }
    suspend fun updateEvent(eventId: Int, name: String, reminder_time: String, description: String): Boolean{
        val convertReminderTime = reverseConvertReminderTime(reminder_time)
        return eventsRepository.updateEvent(eventId, name, convertReminderTime, description)
    }
    suspend fun getEventById(eventId: Int): Event? {
        var event = eventsRepository.getEventById(eventId)
        return if (event != null) {
            event.event_date = convertEventDate(event.event_date)
            event.reminder_time = convertReminderTime(event.reminder_time)
            event
        } else {
            null
        }
    }

    fun convertEventDate(date: String): String {
        val fromFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val toFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val parsedDate = fromFormat.parse(date)
        return toFormat.format(parsedDate)
    }

    fun reverseConvertEventDate(date: String): String {
        return try {
            val fromFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val toFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = fromFormat.parse(date)
            toFormat.format(parsedDate)
        } catch (e: Exception) {
            ""
        }
    }

    fun convertReminderTime(time: String): String {
        val fromFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val toFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val parsedTime = fromFormat.parse(time)
        return toFormat.format(parsedTime)
    }

    fun reverseConvertReminderTime(time: String): String {
        return try {
            val fromFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val toFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val parsedTime = fromFormat.parse(time)
            toFormat.format(parsedTime)
        } catch (e: Exception) {
            ""
        }
    }

    private fun jsonConverter(jsonArray: JsonArray): List<Event> {
        val list = mutableListOf<Event>()
        jsonArray.forEach { jsonElement ->
            val jsonObject = jsonElement.asJsonObject
            val listElement = Event(
                id = jsonObject.get("id").asInt,
                name = jsonObject.get("name").asString,
                reminder_time = convertReminderTime(jsonObject.get("reminder_time").asString),
                description = jsonObject.get("description").asString,
                event_date = convertEventDate(jsonObject.get("event_date").asString)
            )
            list.add(listElement)
        }
        return list
    }
}