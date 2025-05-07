package com.example.gifty

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        // Обработка нажатия на кнопку "Назад"
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()  // Закрытие текущей активности и возврат к MainActivity
            // Убираем анимацию перехода
            overridePendingTransition(0, 0)
        }
        findViewById<TextView>(R.id.myaccount).setOnClickListener {
            val intent = Intent(this, MyAccountActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        findViewById<TextView>(R.id.problems).setOnClickListener {
            val url = "https://docs.google.com/forms/d/e/1FAIpQLSdvzcco5bNdhMdAI6YSUuzmERDHoenCg2UW71SsUM4klbhhXg/viewform?usp=dialog"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}