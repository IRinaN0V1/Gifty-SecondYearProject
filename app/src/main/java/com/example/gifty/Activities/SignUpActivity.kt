package com.example.gifty.Activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.gifty.R
import com.example.gifty.ViewModels.SignUpViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private val viewModel: SignUpViewModel by viewModels()

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var checkPassword: EditText
    private lateinit var errorText: TextView
    private lateinit var signUpButton: ConstraintLayout
    private lateinit var eyeButtonOne: ImageView
    private lateinit var eyeButtonTwo: ImageView
    private lateinit var authButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // Инициализация элементов интерфейса
        email = findViewById(R.id.emailSignUp)
        password = findViewById(R.id.passwordSignUp)
        checkPassword = findViewById(R.id.checkPassword)
        errorText = findViewById(R.id.errorText)
        signUpButton = findViewById(R.id.signUpButton)
        eyeButtonOne = findViewById(R.id.eyeButtonOne)
        eyeButtonTwo = findViewById(R.id.eyeButtonTwo)

        // Кнопка для перехода на экран авторизации
        authButton = findViewById(R.id.authButton)
        // Обработчик нажатия кнопки authButton
        authButton.setOnClickListener {
            // Изменяем цвет текста кнопки при нажатии
            authButton.setTextColor(Color.WHITE)
            // Переход на экран авторизации
            startActivity(Intent(this, SignInActivity::class.java))
            // Удаляем стандартную анимацию перехода
            overridePendingTransition(0, 0)
        }

        // Обработчик кнопки регистрации
        signUpButton.setOnClickListener {
            validateAndSignUp()
        }

        // Нажатие на кнопку просмотра пароля для переключения видимости паролей
        eyeButtonOne.setOnClickListener { togglePasswordVisibility(password, eyeButtonOne) }
        eyeButtonTwo.setOnClickListener { togglePasswordVisibility(checkPassword, eyeButtonTwo) }

        // Установление наблюдателя за полями ввода пароля
        setupPasswordWatcher()

        // Наблюдение за результатом регистрации
        viewModel.signUpResult.observe(this) { result ->
            if (result) {
                Toast.makeText(this, "Вы успешно зарегистрированы!", Toast.LENGTH_LONG).show()
                // Переход на экран авторизации
                startActivity(Intent(this, SignInActivity::class.java))
                overridePendingTransition(0, 0)
            } else {
                Toast.makeText(this, "Пользователь с таким адресом уже зарегистрирован", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Общий слушатель для полей ввода пароля
    private fun setupPasswordWatcher() {
        password.addTextChangedListener(createPasswordTextWatcher(eyeButtonOne))
        checkPassword.addTextChangedListener(createPasswordTextWatcher(eyeButtonTwo))
    }

    // Наблюдатель за полем ввода пароля
    private fun createPasswordTextWatcher(eyeButton: ImageView): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Скрываем/показываем кнопку просмотра пароля в зависимости от наличия текста в поле
                eyeButton.visibility = if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }
        }
    }

    // Переключение видимости пароля
    private fun togglePasswordVisibility(passwordField: EditText, eyeButton: ImageView) {
        // Определяем, показан пароль или скрыт
        val isPasswordVisible = passwordField.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
        // Переключаем тип ввода пароля
        passwordField.inputType = if (isPasswordVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        // Меняем изображение иконки
        eyeButton.setImageResource(if (isPasswordVisible) R.drawable.eye_closed else R.drawable.eye_open)

        // Перемещение курсора в конец строки
        passwordField.setSelection(passwordField.text.length)

        // Автоматически прячем пароль через 1 секунда после отображения
        if (!isPasswordVisible) {
            Handler(Looper.getMainLooper()).postDelayed({
                passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeButton.setImageResource(R.drawable.eye_closed)
                // Возвращаем курсор в конец строки
                passwordField.setSelection(passwordField.text.length)
            }, 1000)
        }
    }

    // Валидация данных перед регистрацией
    private fun validateAndSignUp() {
        val emailText = email.text.toString().trim()
        val passwordText = password.text.toString()
        val checkPasswordText = checkPassword.text.toString()
        // Если данные некорректны
        if (!viewModel.validateSignUp(emailText, passwordText, checkPasswordText)) {
            // Проверка адреса
            if (!viewModel.userInteractor.validateEmail(emailText)) {
                Toast.makeText(this, "Некорректный электронный адрес", Toast.LENGTH_LONG).show()
                return
            }
            // Проверка пароля
            if (!viewModel.userInteractor.validatePassword(passwordText)) {
                errorText.text = "Пароль должен содержать минимум 6 символов,\nвключая строчные и заглавные буквы латинского алфавита и цифры"
                errorText.visibility = View.VISIBLE
                return
            }
            // Проверка совпадения паролей
            if (!viewModel.userInteractor.passwordsMatch(passwordText, checkPasswordText)) {
                errorText.visibility = View.GONE
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Регистрация пользователя
        viewModel.signUp(emailText, passwordText)
    }
}