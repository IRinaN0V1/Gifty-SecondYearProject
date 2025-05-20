package com.example.gifty.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.gifty.R
import com.example.gifty.ViewModels.MyAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyAccountActivity : AppCompatActivity() {
    private val viewModel: MyAccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_account)

        // Получаем данные о пользователе
        val preferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail: TextView = findViewById(R.id.email)
        userEmail.text = preferences.getString("user_email", null)

        // Слушатель для кнопки назад
        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }
        // Случатель для кнопки "Изменить пароль"
        findViewById<TextView>(R.id.changePassword).setOnClickListener {
            // Перенаправление на активити для смены пароля
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
        findViewById<TextView>(R.id.deleteAccount).setOnClickListener { showDeleteConfirmationDialog() }
        val exitBtn = findViewById<ConstraintLayout>(R.id.exitBtn)
        exitBtn.setOnClickListener { showExitConfirmationDialog() }

        // Наблюдаем за результатом удаления аккаунта
        viewModel.deleteResult.observe(this) { result ->
            if (result) {
                // Очищаем сохранённые настройки и выходим из приложения
                clearSharedPrefsAndLogout()
                Toast.makeText(this, "Аккаунт удалён", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Ошибка. Попробуйте снова позже.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Диалоговое окно подтверждения удаления аккаунта
    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Подтверждение")
        builder.setMessage("Вы действительно хотите удалить аккаунт? Все данные будут потеряны.")

        builder.setPositiveButton("Да") { _, _ ->
            val userId = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1)
            if (userId != -1) {
                // Удаляем аккаунт
                viewModel.deleteAccount(userId)
            }
        }
        builder.setNegativeButton("Нет") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    // Диалоговое окно подтверждения выхода из аккаунта
    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Подтверждение")
        builder.setMessage("Вы действительно хотите выйти из аккаунта?")

        builder.setPositiveButton("Да") { _, _ -> clearSharedPrefsAndLogout() }
        builder.setNegativeButton("Нет") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    // Очистка данных о пользователе и выполнение выхода
    private fun clearSharedPrefsAndLogout() {
        // Очищаем все данные пользователя
        viewModel.clearUserPreferences()
        // Перенаправление на вход в систему
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
        overridePendingTransition(0, 0)
    }
}