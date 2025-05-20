package com.example.gifty.Interactors

import com.example.gifty.Api
import com.example.gifty.DataSources.Forms.FormDataSourceImpl
import com.example.gifty.DataSources.Users.UserDataSourceImpl
import com.example.gifty.Repositories.FormRepository
import com.example.gifty.Repositories.UserRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormInteractorTest {
    private lateinit var formInteractor: FormInteractor
    private lateinit var formRepository: FormRepository
    private lateinit var userInteractor: UserInteractor
    private lateinit var userRepository: UserRepository
    private lateinit var api: Api
    private var testFormId = -1
    private var userId = -1

    @Before
    fun setUp() {
        api = Retrofit.Builder()
            .baseUrl("http://192.168.0.100/myApi/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api::class.java)

        val userDataSource = UserDataSourceImpl(api)
        userRepository = UserRepository(userDataSource)
        userInteractor = UserInteractor(userRepository)

        val formDataSource = FormDataSourceImpl(api)
        formRepository = FormRepository(formDataSource)
        formInteractor = FormInteractor(formRepository)

        setupDatabaseForTests()
    }

    private fun setupDatabaseForTests() = runBlocking  {
        val email = "usertest1@gmail.com"
        val password = "Password_123"
        val resultCreate = userInteractor.createUser(email, password)
        assertTrue(resultCreate)

        val user = userInteractor.getUser(email, password)
        if (user != null){
            userId = user.id
        }
    }

    @Test
    fun testCreateForm() = runBlocking {
        val name = "Новая анкета1"
        val image = "ссылка"
        val birthday = "20.05.1990"

        // Создаем анкету и проверяем результат
        val result = formInteractor.createForm(name, image, birthday, userId)
        assertTrue(result)

        // Получаем анкеты для созданного ранее пользователя
        val forms = formInteractor.getFormsByUserId(userId)
        // Проверяем, что список анкет не пустой
        assertTrue(forms.isNotEmpty())

        // Среди анкет находим нужную по названию
        val createdForm = forms.find { it.name == name }
        // Сохраняем ID для дальнейшего удаления в tearDown
        createdForm?.let {
            testFormId = it.id
        }
        assertNotNull(createdForm)
    }

    @Test
    fun testCreateForm_IncorrectData() = runBlocking {
        val name = "Новая анкета1"
        val image = "ссылка"
        val birthday = "20-05-1990"

        // Проверка создания формы
        val result = formInteractor.createForm(name, image, birthday, userId)
        assertFalse(result)

        // Получение форм для проверки, что форма не была создана
        val forms = formInteractor.getFormsByUserId(userId)
        assertTrue(forms.isEmpty())
    }

    @Test
    fun testGetFormById() = runBlocking {
        val name = "Новая анкета2"
        val image = "ссылка"
        val birthday = "20.05.1990"

        // Проверка создания формы
        val result = formInteractor.createForm(name, image, birthday, userId)
        assertTrue(result)

        // Получение форм для проверки, что форма была создана
        val forms = formInteractor.getFormsByUserId(userId)
        assertTrue(forms.isNotEmpty())

        val createdForm = forms.find { it.name == name }
        createdForm?.let {
            testFormId = it.id
        }

        val form = formInteractor.getFormById(testFormId)
        assertNotNull(form)
        assertEquals("Новая анкета2", form?.name)
    }

    @Test
    fun testGetFormsByUserId() = runBlocking {
        val name = "Новая анкета2"
        val image = "ссылка"
        val birthday = "20.05.1990"

        // Проверка создания формы
        val result = formInteractor.createForm(name, image, birthday, userId)
        assertTrue(result)

        val forms = formInteractor.getFormsByUserId(userId)
        assertNotNull(forms)
        assertTrue(forms.isNotEmpty())
    }

    @Test
    fun testGetFormsByUserIdAndName() = runBlocking {
        val name = "Новая анкета3"
        val image = "ссылка"
        val birthday = "20.05.1990"

        // Проверка создания формы
        val resultCreate = formInteractor.createForm(name, image, birthday, userId)
        assertTrue(resultCreate)

        val result = formInteractor.getFormByUserIdAndName(userId, name)
        assertNotNull(result)
        assertTrue(result)
    }

    @Test
    fun testUpdateForm() = runBlocking {
        val name = "Новая анкета2"
        val image = "ссылка"
        val birthday = "20.05.1990"

        // Проверка создания формы
        val resultCreate = formInteractor.createForm(name, image, birthday, userId)
        assertTrue(resultCreate)

        // Получение форм для проверки, что форма была создана
        val forms = formInteractor.getFormsByUserId(userId)
        assertTrue(forms.isNotEmpty())

        val createdForm = forms.find { it.name == name }
        createdForm?.let {
            testFormId = it.id
        }

        val newName = "Новая анкета3"
        val newBirthday = "12.05.2000"
        val result = formInteractor.updateForm(testFormId, newName, newBirthday, "null")
        assertTrue(result)

        // Проверяем, что имя обновилось
        val updatedForm = formInteractor.getFormById(testFormId)
        assertNotNull(updatedForm)
        assertEquals(newName, updatedForm?.name)
    }

    @Test
    fun testDeleteForm() = runBlocking {
        val name = "Новая анкета4"
        val image = "ссылка"
        val birthday = "20.05.1990"

        // Проверка создания формы
        val resultCreate = formInteractor.createForm(name, image, birthday, userId)
        assertTrue(resultCreate)

        // Получение форм для проверки, что форма была создана
        val forms = formInteractor.getFormsByUserId(userId)
        assertTrue(forms.isNotEmpty())

        val createdForm = forms.find { it.name == name }
        createdForm?.let {
            testFormId = it.id
        }

        val result = formInteractor.deleteForm(testFormId)
        assertTrue(result)
    }

    @After
    fun tearDown() = runBlocking {
        val result = api.deleteAllForms()
    }
}