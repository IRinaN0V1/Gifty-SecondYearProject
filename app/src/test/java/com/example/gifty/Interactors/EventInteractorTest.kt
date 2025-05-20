package com.example.gifty.Interactors

import com.example.gifty.Api
import com.example.gifty.DataSources.Events.EventDataSourceImpl
import com.example.gifty.DataSources.Users.UserDataSourceImpl
import com.example.gifty.Repositories.EventRepository
import com.example.gifty.Repositories.UserRepository
import com.google.gson.GsonBuilder
import org.junit.After
import kotlinx.coroutines.runBlocking
import org.junit.Before
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Assert.*

class EventInteractorTest {
    private lateinit var eventInteractor: EventInteractor
    private lateinit var eventRepository: EventRepository
    private lateinit var userInteractor: UserInteractor
    private lateinit var userRepository: UserRepository
    private lateinit var api: Api
    private var testEventId = -1
    private var userId = -1

    @Before
    fun setUp() {
        api = Retrofit.Builder()
            .baseUrl("http://192.168.0.100/myApi/v1/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(Api::class.java)

        val userDataSource = UserDataSourceImpl(api)
        userRepository = UserRepository(userDataSource)
        userInteractor = UserInteractor(userRepository)

        val eventDataSource = EventDataSourceImpl(api)
        eventRepository = EventRepository(eventDataSource)
        eventInteractor = EventInteractor(eventRepository)

        setupDatabaseForTests()
    }

    private fun setupDatabaseForTests() = runBlocking {
        val email = "usertest1@gmail.com"
        val password = "Password_123"
        val resultCreate = userInteractor.createUser(email, password)

        val user = userInteractor.getUser(email, password)
        if (user != null){
            userId = user.id
        }
    }

    @Test
    fun testCreateEvent() = runBlocking {
        val name = "Новое событие"
        val reminderTime = "12.05.2025 13:00"
        val description = "Описание нового события"
        val eventDate = "12.05.2025"

        // Проверка создания события
        val result = eventInteractor.createEvent(userId, name, reminderTime, description, eventDate)
        assertTrue(result)

        // Проверка, что событие было создано
        val events = eventInteractor.getEventsByUserId(userId)
        assertTrue(events.isNotEmpty())

        val createdEvent = events.find { it.name == name }
        assertNotNull(createdEvent)
    }

    @Test
    fun testCreateEvent_IncorrectEventDate() = runBlocking {
        val name = "Новое событие"
        val reminderTime = "12.05.2025 13:00"
        val description = "Описание нового события"
        val eventDate = "12-05-2025"

        // Проверка создания события
        val result = eventInteractor.createEvent(userId, name, reminderTime, description, eventDate)
        assertFalse(result)

        // Проверка, что событие было создано
        val events = eventInteractor.getEventsByUserId(userId)
        assertFalse(events.isNotEmpty())
    }

    @Test
    fun testCreateEvent_IncorrectReminderTime() = runBlocking {
        val name = "Новое событие"
        val reminderTime = "12-05-2025 13:00"
        val description = "Описание нового события"
        val eventDate = "12.05.2025"

        // Проверка создания события
        val result = eventInteractor.createEvent(userId, name, reminderTime, description, eventDate)
        assertFalse(result)

        // Проверка, что событие было создано
        val events = eventInteractor.getEventsByUserId(userId)
        assertFalse(events.isNotEmpty())
    }

    @Test
    fun testGetEventById() = runBlocking {
        val name = "Новое событие1"
        val reminderTime = "12.05.2025 13:00"
        val description = "Описание нового события"
        val eventDate = "12.05.2025"

        // Проверка создания события
        val result = eventInteractor.createEvent(userId, name, reminderTime, description, eventDate)
        assertTrue(result)

        // Проверка, что событие было создано
        val events = eventInteractor.getEventsByUserId(userId)
        assertTrue(events.isNotEmpty())

        val createdEvent = events.find { it.name == name }
        assertNotNull(createdEvent)

        createdEvent?.let {
            testEventId = it.id
        }
        val event = eventInteractor.getEventById(testEventId)
        assertNotNull(event)
        assertEquals("Новое событие1", event?.name)
    }

    @Test
    fun testGetEventsByUserId() = runBlocking {
        val name = "Новое событие"
        val reminderTime = "12.05.2025 13:00"
        val description = "Описание нового события"
        val eventDate = "12.05.2025"

        // Проверка создания события
        val resultCreate = eventInteractor.createEvent(userId, name, reminderTime, description, eventDate)
        assertTrue(resultCreate)

        val events = eventInteractor.getEventsByUserId(userId)
        assertNotNull(events)
        assertTrue(events.isNotEmpty())
    }

    @Test
    fun testUpdateEvent() = runBlocking {
        val name = "Новое событие2"
        val reminderTime = "12.05.2025 13:00"
        val description = "Описание нового события"
        val eventDate = "12.05.2025"

        // Проверка создания события
        val resultCreate = eventInteractor.createEvent(userId, name, reminderTime, description, eventDate)
        assertTrue(resultCreate)

        // Проверка, что событие было создано
        val events = eventInteractor.getEventsByUserId(userId)
        assertTrue(events.isNotEmpty())

        val createdEvent = events.find { it.name == name }
        assertNotNull(createdEvent)
        createdEvent?.let {
            testEventId = it.id
        }
        val newName = "Обновленное событие"
        val newReminderTime = "12.05.2025 15:00"
        val newDescription = "Новое описание для события"

        val result = eventInteractor.updateEvent(testEventId, newName, newReminderTime, newDescription)
        assertTrue(result)

        // Проверка, что событие обновилось
        val updatedEvent = eventInteractor.getEventById(testEventId)
        assertNotNull(updatedEvent)
        assertEquals(newName, updatedEvent?.name)
    }

    @Test
    fun testDeleteEvent() = runBlocking {
        val name = "Новое событие3"
        val reminderTime = "12.05.2025 13:00"
        val description = "Описание нового события"
        val eventDate = "12.05.2025"

        // Проверка создания события
        val resultCreate = eventInteractor.createEvent(userId, name, reminderTime, description, eventDate)
        assertTrue(resultCreate)

        // Проверка, что событие было создано
        val events = eventInteractor.getEventsByUserId(userId)
        assertTrue(events.isNotEmpty())

        val createdEvent = events.find { it.name == name }
        assertNotNull(createdEvent)
        createdEvent?.let {
            testEventId = it.id
        }

        val result = eventInteractor.deleteEvent(testEventId)
        assertTrue(result)
    }

    @Test
    fun testGetEventByInvalidId() = runBlocking {
        val event = eventInteractor.getEventById(-1)  // Пытаемся получить несуществующее событие
        assertNull(event)  // Ожидаем, что событие будет null
    }

    @Test
    fun testDeleteEventWithInvalidId() = runBlocking {
        val result = eventInteractor.deleteEvent(-1)  // Пытаемся удалить несуществующее событие
        assertFalse(result)  // Ожидаем, что удаление завершится неудачно
    }

    @Test
    fun testGetEventsByInvalidUserId() = runBlocking {
        val events = eventInteractor.getEventsByUserId(-1)  // Пытаемся получить события для несуществующего пользователя
        assertNotNull(events)  // Ожидаем, что метод не вернет null
        assertTrue(events.isEmpty())
    }

    @After
    fun tearDown() = runBlocking {
        val result = api.deleteAllEvents()
    }
}