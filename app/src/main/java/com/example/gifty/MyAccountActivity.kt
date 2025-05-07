package com.example.gifty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
class MyAccountActivity : AppCompatActivity() {
    @Inject
    lateinit var api: Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_account)
        val preferences = getSharedPreferences("PC", Context.MODE_PRIVATE)
        val email = preferences.getString("Email", null)!!

        val userEmail: TextView = findViewById(R.id.email)

        userEmail.text = email

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()  // Закрытие текущей активности и возврат к MainActivity
            // Убираем анимацию перехода
            overridePendingTransition(0, 0)
        }
        findViewById<TextView>(R.id.changePassword).setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        findViewById<TextView>(R.id.deleteAccount).setOnClickListener {
            showDeleteConfirmationDialog()
        }
        // Обработчик выхода из аккаунта
        val exitBtn = findViewById<ConstraintLayout>(R.id.exitBtn)
        exitBtn.setOnClickListener {
            showExitConfirmationDialog()
        }
    }
    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Подтверждение")
        builder.setMessage("Вы уверены, что хотите удалить аккаунт без возможности восстановления?\nДанные аккаунта будут утрачены.")

        builder.setPositiveButton("Да") { _, _ ->
            deleteAccount()
        }

        builder.setNegativeButton("Нет") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Подтверждение")
        builder.setMessage("Вы уверены, что хотите выйти из аккаунта?")

        builder.setPositiveButton("Да") { _, _ ->
            clearSharedPrefsAndLogout()
        }

        builder.setNegativeButton("Нет") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun deleteAccount(){
        val preferences = getSharedPreferences("PC", Context.MODE_PRIVATE)
        val user = preferences.getInt("UserId", -1)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.deleteUser(user)
                val jsonResponse = response.body()
                if (jsonResponse != null) {
                    val message = jsonResponse.get("message").asString
                    if (!jsonResponse.get("error").asBoolean){
                        finish()
                        overridePendingTransition(0, 0)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MyAccountActivity, message, Toast.LENGTH_LONG).show()
                    }

                }
            } catch (e: Exception) {
                Log.e("MyLog", e.message ?: "Unknown exception")
            }
        }

        // Полностью очищаем настройки пользователя
        with(preferences.edit()) {
            clear()
            apply()
        }

        // Создаем Intent для перехода на активность авторизации
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)

        // Завершаем текущую активность после перехода
        finish()
    }


    private fun clearSharedPrefsAndLogout() {
        val preferences = getSharedPreferences("PC", Context.MODE_PRIVATE)

        // Полностью очищаем настройки пользователя
        with(preferences.edit()) {
            clear()
            apply()
        }

        // Создаем Intent для перехода на активность авторизации
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)

        // Завершаем текущую активность после перехода
        finish()
    }
}