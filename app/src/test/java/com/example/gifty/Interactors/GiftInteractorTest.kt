package com.example.gifty.Interactors

import com.example.gifty.Api
import com.example.gifty.DataSources.Forms.FormDataSourceImpl
import com.example.gifty.DataSources.Gifts.GiftDataSourceImpl
import com.example.gifty.DataSources.Users.UserDataSourceImpl
import com.example.gifty.Repositories.FormRepository
import com.example.gifty.Repositories.GiftRepository
import com.example.gifty.Repositories.UserRepository
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GiftInteractorTest {
    private lateinit var formInteractor: FormInteractor
    private lateinit var formRepository: FormRepository
    private lateinit var userInteractor: UserInteractor
    private lateinit var userRepository: UserRepository
    private lateinit var giftInteractor: GiftInteractor
    private lateinit var giftRepository: GiftRepository
    private lateinit var api: Api
    private var userId = -1
    private var testGiftId = 1  // Для хранения ID тестируемого подарка
    private var testFormId = -1   // Для хранения ID тестируемой формы

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

        // Инициализация репозитория и интерактора
        val formDataSource = FormDataSourceImpl(api)
        formRepository = FormRepository(formDataSource)
        formInteractor = FormInteractor(formRepository)

        val giftDataSource = GiftDataSourceImpl(api)
        giftRepository = GiftRepository(giftDataSource)
        giftInteractor = GiftInteractor(giftRepository)

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

        val name = "Новая анкета1"
        val image = "ссылка"
        val birthday = "20.05.1990"

        // Проверка создания формы
        val result = formInteractor.createForm(name, image, birthday, userId)
        assertTrue(result)

        // Получение форм для проверки, что форма была создана
        val forms = formInteractor.getFormsByUserId(userId)
        val createdForm = forms.find { it.name == name }
        createdForm?.let {
            testFormId = it.id
        }
    }

    @Test
    fun testAddGiftToForm() = runBlocking {
        assertTrue(testGiftId != -1)
        assertTrue(testFormId != -1)

        val result = giftInteractor.addGiftToForm(testGiftId, testFormId)
        assertTrue(result)
    }

    @Test
    fun testGetGifts() = runBlocking {
        val gifts = giftInteractor.getGifts()
        assertNotNull(gifts)
        assertTrue(gifts.isNotEmpty())

        gifts.forEach {
            assertNotNull(it.id)
            assertNotNull(it.name)
        }
    }

    @Test
    fun testGetSelectedGifts() = runBlocking {
        val resultAdd = giftInteractor.addGiftToForm(2, testFormId)
        assertTrue(resultAdd)
        assertTrue(testFormId != -1)
        val selectedGifts = giftInteractor.getSelectedGifts(testFormId)
        assertNotNull(selectedGifts)
        assertTrue(selectedGifts.isNotEmpty())
    }

    @Test
    fun testDeleteSelectedGifts() = runBlocking {
        val resultAdd = giftInteractor.addGiftToForm(3, testFormId)
        assertTrue(resultAdd)
        assertTrue(testFormId != -1)
        val result = giftInteractor.deleteSelectedGifts(testFormId, "$testGiftId")
        assertTrue(result)
    }

    @Test
    fun testGetGiftById() = runBlocking {
        assertTrue(testGiftId != -1)
        val gift = giftInteractor.getGiftById(testGiftId)
        assertNotNull(gift)
        assertEquals(testGiftId, gift?.id)
    }

    @Test
    fun testGetFoundGifts() = runBlocking {
        val genderIds = "1,3"
        val ageCategoryId = 5
        val hobbies = "1"
        val professions = "3,4,5"
        val holidays = ""
        assertTrue(testGiftId != -1)
        val gifts = giftInteractor.getFoundGifts(genderIds, ageCategoryId, hobbies, professions, holidays)
        assertNotNull(gifts)
        assertFalse(gifts!!.isEmpty())
    }

    @Test
    fun testGetSelectedGiftByGiftIdAndFormId() = runBlocking {
        val resultAdd = giftInteractor.addGiftToForm(4, testFormId)
        assertTrue(resultAdd)
        assertTrue(testFormId != -1)
        val result = giftInteractor.getSelectedGiftByGiftIdAndFormId(testGiftId, testFormId)
        assertFalse(result)
    }

    @After
    fun tearDown() = runBlocking {

    }
}