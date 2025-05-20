package com.example.gifty.Activities
import android.content.Context
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
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.gifty.ViewModels.SignInViewModel
import androidx.activity.viewModels
import com.example.gifty.MainActivity
import com.example.gifty.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {
    private val viewModel: SignInViewModel by viewModels()

    private lateinit var registrButton: TextView
    private lateinit var signInButton: ConstraintLayout
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var eyeButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)

        // Проверяем наличие сохраненных данных
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        // Если userId не -1, значит пользователь уже авторизован
        if (userId != -1) {
            // Переходим на MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Закрываем текущую активность
            return // Завершаем выполнение onCreate
        }

        registrButton = findViewById(R.id.registrButton)
        signInButton = findViewById(R.id.signInButton)
        email = findViewById(R.id.emailSignIn)
        password = findViewById(R.id.passwordSignIn)
        eyeButton = findViewById(R.id.eyeButton)

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
            }, 1000)
        }

        // Обработчик нажатия для кнопки авторизации
        signInButton.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = password.text.toString()
            // Авторизация пользователя
            viewModel.signIn(emailText, passwordText)
        }

        // Наблюдатель за результатом авторизации пользователя
        viewModel.signInResult.observe(this) { user ->
            if (user == null) {
                Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(this, "Добро пожаловать в Gifty!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(0, 0)
            }
        }

        // Обработка нажатия на кнопку перехода на страницу регистрации
        registrButton.setOnClickListener {
            registrButton.setTextColor(Color.WHITE)
            startActivity(Intent(this, SignUpActivity::class.java))
            overridePendingTransition(0, 0)
        }
    }
}