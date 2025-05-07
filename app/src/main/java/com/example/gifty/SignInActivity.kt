package com.example.gifty

import android.content.Context
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

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {
    @Inject
    lateinit var api: Api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Кнопка для перехода на экран регистрации
        var registrButton:TextView = findViewById(R.id.registrButton)
        //Кнопка авторизации
        var signInButton:ConstraintLayout = findViewById(R.id.signInButton)
        //Поле для ввода электронного адреса
        var email: EditText = findViewById(R.id.emailSignIn)
        //Поле для ввода пароля
        var password:EditText = findViewById(R.id.passwordSignIn)
        //Кнопка для просмотра пароля
        var eyeButton: ImageView = findViewById(R.id.eyeButton)


        //Создаем файл PC, в котором будут храниться настройки приложения, доступ к файлу только из приложения
        var  sp = getSharedPreferences("PC", Context.MODE_PRIVATE)
        //Проверяем, авторизировался пользователь ранее или нет
        if (sp.getString("TY", "-9") != "-9"){
            startActivity(Intent(this, MainActivity::class.java))
            // Убираем анимацию перехода
            overridePendingTransition(0, 0)
        }
        else{
            // Слушатель для отслеживания изменений текста в EditText
            password.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length!! > 0) {
                        // Показываем кнопку, если текст введен
                        eyeButton.visibility = View.VISIBLE
                    } else {
                        // Скрываем кнопку, если текст удалён
                        eyeButton.visibility = View.GONE
                    }
                }
            })
            //Обработчик нажатия кнопки registrButton
            registrButton.setOnClickListener{
                //При нажатии подсвечиваем кнопку
                registrButton.setTextColor(Color.WHITE)
                //Переходим на экран регистрации
                var intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
                // Убираем анимацию перехода
                overridePendingTransition(0, 0)
            }
            //Обработчик нажатия кнопки eyeButton
            eyeButton.setOnClickListener {
                // Показать пароль
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                eyeButton.setImageResource(R.drawable.eye_open)
                // Перемещаем курсор в конец строки
                val passwordText = password.text.toString()
                password.setSelection(passwordText.length)
                // Таймер для автоматического скрытия пароля через 1 секунду
                Handler(Looper.getMainLooper()).postDelayed({
                    password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    eyeButton.setImageResource(R.drawable.eye_closed)
                    // Перемещаем курсор в конец строки
                    val passwordText = password.text.toString()
                    password.setSelection(passwordText.length)
                }, 1000) // Задержка 1 секунда (1000 миллисекунд)
            }
            //Обработчик нажатия кнопки авторизации
            signInButton.setOnClickListener{
                //Проверка, что электронный адрес введен
                if (email.text.isEmpty() || email.text.toString() == " "){
                    Toast.makeText(this, "Укажите электронный адрес", Toast.LENGTH_LONG).show()
                }
                //Проверка, что пароль введен
                else if (password.text.isEmpty()){
                    Toast.makeText(this, "Укажите пароль", Toast.LENGTH_LONG).show()
                }
                else{
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = api.getUser(email.text.toString(), password.text.toString())
                            withContext(Dispatchers.Main) {
                                val jsonResponse = response.body()
                                if (jsonResponse != null && !jsonResponse.get("error").asBoolean) {
                                    val userJson = jsonResponse.getAsJsonObject("user")
                                    val userId = userJson.get("id").asInt
                                    val userEmail = userJson.get("email").asString
                                    val userPassword = userJson.get("password").asString

                                    // Успех, пользователь найден
                                    sp.edit().putInt("UserId", userId).apply()
                                    Log.d("MyLog", "$userId")
                                    sp.edit().putString("Email", userEmail).apply()
                                    sp.edit().putString("Password", userPassword).apply()
                                    startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                                    overridePendingTransition(0, 0)
                                } else {
                                    // Пользователь не найден
                                    Toast.makeText(this@SignInActivity, "Неверный эл. адрес или пароль", Toast.LENGTH_LONG).show()
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
}