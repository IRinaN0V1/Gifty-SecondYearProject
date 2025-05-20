package com.example.gifty.Interactors

import com.example.gifty.Api
import com.example.gifty.DataSources.Categories.CategoryDataSourceImpl
import com.example.gifty.Repositories.CategoryDataRepository
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.junit.Assert.*

class CategoryInteractorTest {
    private lateinit var categoryInteractor: CategoryInteractor
    private lateinit var categoryRepository: CategoryDataRepository
    private lateinit var api: Api

    @Before
    fun setUp() {
        api = Retrofit.Builder()
            .baseUrl("http://192.168.0.100/myApi/v1/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(Api::class.java)

        val categoryDataSource = CategoryDataSourceImpl(api)
        categoryRepository = CategoryDataRepository(categoryDataSource)
        categoryInteractor = CategoryInteractor(categoryRepository)
    }

    @Test
    fun testGetHolidays() = runBlocking {
        val holidays = categoryInteractor.getHolidays()
        assertNotNull(holidays)
        assertTrue(holidays.isNotEmpty())
        holidays.forEach {
            assertNotNull(it.id)
            assertNotNull(it.name)
        }
    }

    @Test
    fun testGetHobbies() = runBlocking {
        val hobbies = categoryInteractor.getHobbies()
        assertNotNull(hobbies)  // Проверяем, что список не null
        assertTrue(hobbies.isNotEmpty())  // Проверка на наличие данных
        hobbies.forEach {
            assertNotNull(it.id)  // Проверяем, что у каждого элемента есть ID
            assertNotNull(it.name)  // Проверяем, что каждый элемент имеет имя
        }
    }

    @Test
    fun testGetProfessions() = runBlocking {
        val professions = categoryInteractor.getProfessions()
        assertNotNull(professions)  // Проверяем, что список не null
        assertTrue(professions.isNotEmpty())  // Проверка на наличие данных
        professions.forEach {
            assertNotNull(it.id)  // Проверяем, что у каждого элемента есть ID
            assertNotNull(it.name)  // Проверяем, что каждый элемент имеет имя
        }
    }

    @Test
    fun testGetAgeCategoryByAge() = runBlocking {
        val age = 25
        val ageCategory = categoryInteractor.getAgeCategoryByAge(age)
        assertTrue(ageCategory >= 0)  // Предполагаем, что возрастная категория не может быть отрицательной
    }

    @After
    fun tearDown() = runBlocking {

    }
}