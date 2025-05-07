package com.example.gifty

import android.content.Context
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

private fun isValidPassword(password: String): Boolean {
    val regex = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)".toRegex()
    return regex.containsMatchIn(password)
}
@AndroidEntryPoint
class ChangePasswordActivity : AppCompatActivity() {
    @Inject
    lateinit var api: Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()  // Закрытие текущей активности и возврат к MainActivity
            // Убираем анимацию перехода
            overridePendingTransition(0, 0)
        }
        var password: EditText = findViewById(R.id.passwordSignUp)
        //Текст ошибки при неверном вводе пароля
        val errorText = findViewById<TextView>(R.id.errorText)
        //Поле для повторного ввода пароля
        var checkPassword: EditText = findViewById(R.id.checkPassword)
        //Кнопка регистрации
        var savePassword: ConstraintLayout = findViewById(R.id.savePassword)
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

        savePassword.setOnClickListener{
            if (password.text.isEmpty()){
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
                val sp = getSharedPreferences("PC", Context.MODE_PRIVATE)
                val user = sp.getInt("UserId", -1)
                // Скрываем текст ошибки, если пароль корректный
                errorText.visibility = View.GONE

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = api.updateUserPassword(user, password.text.toString())
                        val jsonResponse = response.body()
                        if (jsonResponse != null) {
                            val message = jsonResponse.get("message").asString
                            if (!jsonResponse.get("error").asBoolean){
                                sp.edit().putString("Password", password.text.toString()).apply()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@ChangePasswordActivity, message, Toast.LENGTH_LONG).show()
                                }
                            }
                            else{
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@ChangePasswordActivity, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MyLog", "Exception occurred: ${e.message}")
                    }
                }
            }
        }
    }
}