package com.example.gifty.Activities

import android.content.Context
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
import com.example.gifty.Api
import com.example.gifty.R
import com.example.gifty.ViewModels.ChangePasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangePasswordActivity : AppCompatActivity() {
    private val viewModel: ChangePasswordViewModel by viewModels()
    @Inject
    lateinit var api: Api
    private lateinit var eyeButtonOne: ImageView
    private lateinit var eyeButtonTwo: ImageView
    private lateinit var password: EditText
    private lateinit var checkPassword: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            // Закрытие текущей активности и возврат к MainActivity
            finish()
            // Убираем анимацию перехода
            overridePendingTransition(0, 0)
        }
        password = findViewById(R.id.passwordSignUp)
        //Текст ошибки при неверном вводе пароля
        val errorText = findViewById<TextView>(R.id.errorText)
        //Поле для повторного ввода пароля
        checkPassword = findViewById(R.id.checkPassword)
        //Кнопка регистрации
        var savePassword: ConstraintLayout = findViewById(R.id.savePassword)
        //Кнопки для просмотра пароля
        eyeButtonOne = findViewById(R.id.eyeButtonOne)
        eyeButtonTwo = findViewById(R.id.eyeButtonTwo)

        // Устанавливаем обработчики нажатий на кнопки просмотра паролей
        eyeButtonOne.setOnClickListener { togglePasswordVisibility(password, eyeButtonOne) }
        eyeButtonTwo.setOnClickListener { togglePasswordVisibility(checkPassword, eyeButtonTwo) }

        // Общий слушатель для отслеживания изменений текста в полях пароля
        setupPasswordWatcher()

        // Наблюдение за результатом изменения пароля
        viewModel.changePasswordResult.observe(this) { result ->
            if (result) {
                Toast.makeText(this, "Пароль изменен!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Вы указали текущий пароль, пожалуйста, укажите новый", Toast.LENGTH_LONG).show()
            }
        }

        // Обработка нажатия на кнопку сохранения пароля
        savePassword.setOnClickListener {
            // Получаем текст нового пароля
            val passwordText = password.text.toString()
            // Получаем текст повторного пароля
            val checkPasswordText = checkPassword.text.toString()
            val userId = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1)
            // Проверяем пароль на корректность
            if (!viewModel.userInteractor.validatePassword(passwordText)) {
                errorText.text = "Пароль должен содержать минимум 6 символов,\nвключая строчные и заглавные буквы латинского алфавита и цифры"
                errorText.visibility = View.VISIBLE
            } else if (!viewModel.userInteractor.passwordsMatch(passwordText, checkPasswordText)) {
                errorText.visibility = View.GONE
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_LONG).show()
            } else{
                // Изменение пароля
                viewModel.changePassword(userId, checkPasswordText)
            }
        }
    }

    private fun setupPasswordWatcher() {
        // Подключаем слушатели изменений текста для паролей
        password.addTextChangedListener(createPasswordTextWatcher(eyeButtonOne))
        checkPassword.addTextChangedListener(createPasswordTextWatcher(eyeButtonTwo))
    }

    private fun createPasswordTextWatcher(eyeButton: ImageView): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Показываем или скрываем кнопку просмотра пароля в зависимости от наличия текста
                eyeButton.visibility = if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }
        }
    }

    private fun togglePasswordVisibility(passwordField: EditText, eyeButton: ImageView) {
        // Проверяем, виден ли сейчас пароль
        val isPasswordVisible = passwordField.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
        // Меняем тип поля для отображения/скрытия пароля
        passwordField.inputType = if (isPasswordVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        // Меняем иконку кнопки
        eyeButton.setImageResource(if (isPasswordVisible) R.drawable.eye_closed else R.drawable.eye_open)

        // Перемещаем курсор в конец строки
        passwordField.setSelection(passwordField.text.length)

        // Таймер для автоматического скрытия пароля через 1 секунду
        if (!isPasswordVisible) {
            Handler(Looper.getMainLooper()).postDelayed({
                passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeButton.setImageResource(R.drawable.eye_closed)
                // Перемещаем курсор в конец строки
                passwordField.setSelection(passwordField.text.length)
            }, 1000)
        }
    }
}