package com.example.gifty.Interactors

import com.example.gifty.Api
import com.example.gifty.DataSources.Users.UserDataSourceImpl
import com.example.gifty.Repositories.UserRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class UserInteractorTest {
    private lateinit var userInteractor: UserInteractor
    private lateinit var userRepository: UserRepository
    private lateinit var api: Api

    @Before
    fun setUp() {
        api = Retrofit.Builder()
            .baseUrl("http://192.168.0.100/myApi/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api::class.java)

        // Инициализация репозитория и интерактора
        val userDataSource = UserDataSourceImpl(api)
        userRepository = UserRepository(userDataSource)
        userInteractor = UserInteractor(userRepository)
    }

    @After
    fun tearDown() = runBlocking {
        val result = api.deleteAllUsers()
    }

    @Test
    fun testCreateUser() = runBlocking {
        val email = "usertest1@gmail.com"
        val password = "Password_123"

        // Проверка создания пользователя
        val result = userInteractor.createUser(email, password)
        assertTrue(result)

        // Проверка, что пользователь был создан
        val user = userInteractor.getUser(email, password)
        assertNotNull(user)
        assertEquals(email, user?.email)
    }

    @Test
    fun testUpdateUser() = runBlocking {
        val email = "usertest1@gmail.com"
        val oldPassword = "Password_123"
        val newPassword = "NewPassword_456!"

        // Проверка создания пользователя
        val createResult = userInteractor.createUser(email, oldPassword)
        assertTrue(createResult)

        // Проверка, что пользователь был создан
        val user = userInteractor.getUser(email, oldPassword)
        assertNotNull(user)

        // Получаем userId для обновления пароля
        val userId = user?.id ?: throw Exception("User not found")

        // Обновление пароля
        val updateResult = userInteractor.updateUser(userId, newPassword)
        assertTrue(updateResult)

        // Проверяем, что старый пароль больше не работает
        val oldUser = userInteractor.getUser(email, oldPassword)
        assertNull(oldUser)

        // Проверяем, что новый пароль работает
        val newUser = userInteractor.getUser(email, newPassword)
        assertNotNull(newUser)
    }

    @Test
    fun testGetUser() = runBlocking {
        val email = "usertest2@gmail.com"
        val password = "Password_123"
        val result = userInteractor.createUser(email, password)

        val user = userInteractor.getUser(email, password)
        assertNotNull(user)
        assertEquals(email, user?.email)
    }

    @Test
    fun testDeleteUser() = runBlocking {
        val email = "usertest2@gmail.com"
        val password = "Password_123"
        userInteractor.createUser(email, password)

        val user = userInteractor.getUser(email, password)!!

        val result = userInteractor.deleteUser(user.id)
        assertTrue(result)
    }

    @Test
    fun testCorrectEmail() {
        assertTrue(userInteractor.validateEmail("valid@example.com"))
    }

    @Test
    fun testIncorrectEmail() {
        assertFalse(userInteractor.validateEmail("valid@examplecom"))
        assertFalse(userInteractor.validateEmail("invalidemailcom"))
    }

    @Test
    fun testEmptyEmail() {
        assertFalse(userInteractor.validateEmail(""))
    }

    @Test
    fun testValidatePassword() {
        assertTrue(userInteractor.validatePassword("Valid123"))
        assertFalse(userInteractor.validatePassword("short"))
    }

    @Test
    fun testPasswordsMatch() {
        assertTrue(userInteractor.passwordsMatch("Password123", "Password123"))
        assertFalse(userInteractor.passwordsMatch("Password123", "DifferentPassword"))
    }
}