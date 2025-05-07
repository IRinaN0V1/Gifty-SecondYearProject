package com.example.gifty

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// Функция для проверки пароля
private fun isValidPassword(password: String): Boolean {
    val regex = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)".toRegex()
    return regex.containsMatchIn(password)
}
@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {
    @Inject
    lateinit var api: Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Кнопка для перехода на экран авторизации
        var authButton: TextView = findViewById(R.id.authButton)
        //Обработчик нажатия кнопки authButton
        authButton.setOnClickListener{
            //При нажатии подсвечиваем кнопку
            authButton.setTextColor(Color.WHITE)
            //Переходим на экран авторизации
            var intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            // Убираем анимацию перехода
            overridePendingTransition(0, 0)
        }
        //Поле для ввода электронного адреса
        var email:TextView = findViewById(R.id.emailSignUp)
        //Поле для ввода пароля
        var password:EditText = findViewById(R.id.passwordSignUp)
        //Текст ошибки при неверном вводе пароля
        val errorText = findViewById<TextView>(R.id.errorText)
        //Поле для повторного ввода пароля
        var checkPassword:EditText = findViewById(R.id.checkPassword)
        //Кнопка регистрации
        var signUpButton:ConstraintLayout = findViewById(R.id.signUpButton)
        //Кнопки для просмотра пароля
        var eyeButtonOne: ImageView = findViewById(R.id.eyeButtonOne)
        var eyeButtonTwo: ImageView = findViewById(R.id.eyeButtonTwo)
        // Слушатель для отслеживания изменений при вводе пароля
        password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! > 0) {
                    // Показываем кнопку, если текст введен
                    eyeButtonOne.visibility = View.VISIBLE
                } else {
                    // Скрываем кнопку, если текст удалён
                    eyeButtonOne.visibility = View.GONE
                }
            }
        })
        // Слушатель для отслеживания изменений текста при повторном вводе пароля
        checkPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! > 0) {
                    // Показываем кнопку, если текст введен
                    eyeButtonTwo.visibility = View.VISIBLE
                } else {
                    // Скрываем кнопку, если текст удалён
                    eyeButtonTwo.visibility = View.GONE
                }
            }
        })
        //Обработчик нажатия кнопки eyeButton
        eyeButtonOne.setOnClickListener {
            // Показать пароль
            password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            eyeButtonOne.setImageResource(R.drawable.eye_open)
            // Перемещаем курсор в конец строки
            val passwordText = password.text.toString()
            password.setSelection(passwordText.length)
            // Таймер для автоматического скрытия пароля через 1 секунду
            Handler(Looper.getMainLooper()).postDelayed({
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeButtonOne.setImageResource(R.drawable.eye_closed)
                // Перемещаем курсор в конец строки
                val passwordText = password.text.toString()
                password.setSelection(passwordText.length)
            }, 1000) // Задержка 1 секунда (1000 миллисекунд)
        }
        //Обработчик нажатия кнопки eyeButton
        eyeButtonTwo.setOnClickListener {
            // Показать пароль
            checkPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            eyeButtonTwo.setImageResource(R.drawable.eye_open)
            // Перемещаем курсор в конец строки
            val passwordText = checkPassword.text.toString()
            checkPassword.setSelection(passwordText.length)
            // Таймер для автоматического скрытия пароля через 1 секунду
            Handler(Looper.getMainLooper()).postDelayed({
                checkPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeButtonTwo.setImageResource(R.drawable.eye_closed)
                // Перемещаем курсор в конец строки
                val passwordText = checkPassword.text.toString()
                checkPassword.setSelection(passwordText.length)
            }, 1000) // Задержка 1 секунда (1000 миллисекунд)
        }
        //Обработчик нажатия кнопки регистрации
        signUpButton.setOnClickListener{
            //Проверка, что электронный адрес введен
            if (email.text.isEmpty() || email.text.toString() == " "){
                Toast.makeText(this, "Укажите электронный адрес", Toast.LENGTH_LONG).show()
            }
            //Проверка, что электронный адрес содержит символ @ и точку
            else if (!email.text.toString().contains("@") || !email.text.toString().contains(".")){
                Toast.makeText(this, "Электронный адрес указан некорректно", Toast.LENGTH_LONG).show()
            }
            //Проверка, что пароль введен
            else if (password.text.isEmpty()){
                Toast.makeText(this, "Укажите пароль", Toast.LENGTH_LONG).show()
            }
            //Проверка, что пароль соответсвует требованиям
            else if (password.text.length < 6 || !isValidPassword(password.text.toString())) {
                errorText.text = "Пароль должен содержать минимум 6 символов,\nвключая строчные и заглавные буквы латинского алфавита и цифры"
                errorText.visibility = View.VISIBLE // Выводим текст ошибки
            }
            //Проверка, что пароли совпадают
            else if (password.text.toString() != checkPassword.text.toString() ){
                Toast.makeText(this, "Пароли не совпадают. Повторите пароль еще раз", Toast.LENGTH_LONG).show()
                errorText.visibility = View.GONE // Скрываем текст ошибки, если пароль корректный
            }
            //Успешная регистрация и переход на главную страницу приложения
            else{
                // Скрываем текст ошибки, если пароль корректный
                errorText.visibility = View.GONE

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = api.getUserByEmail(email.text.toString())
                        val jsonResponse = response.body()
                        if (jsonResponse != null) {
                            val message = jsonResponse.get("message").asString
                            if (!jsonResponse.get("flag").asBoolean){
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@SignUpActivity, message, Toast.LENGTH_LONG).show()
                                }
                                CoroutineScope(Dispatchers.IO).launch {
                                    val createUserResponse = api.createUser(email.text.toString(), password.text.toString())
                                }
                                startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                                overridePendingTransition(0, 0)
                            } else{
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@SignUpActivity, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MyLog", "Exception occurred in ${javaClass.simpleName}: ${e.message}")
                    }
                }
            }
        }
    }
}