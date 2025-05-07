package com.example.gifty

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class GiftActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gift)

        val name = intent.getStringExtra("giftName")
        val description = intent.getStringExtra("giftDescription")
        val imageUrl = intent.getStringExtra("giftImage")
        val id = intent.getIntExtra("giftId", -1)

        val giftName: TextView = findViewById(R.id.giftName)
        val giftDescription: TextView = findViewById(R.id.description)
        val image: ImageView = findViewById(R.id.image)
        val addGiftToFormBtn: ConstraintLayout = findViewById(R.id.addGiftToForm)
        val backBtn: ImageView = findViewById(R.id.backButton)

        giftName.text = name
        giftDescription.text = description

        // Загрузка изображения
        Glide.with(this).load(imageUrl).into(image)

        addGiftToFormBtn.setOnClickListener{
            val intent = Intent(this, ChoseFormActivity::class.java)
            intent.putExtra("giftId", id)
            startActivity(intent)
            this.overridePendingTransition(0, 0)
        }

        // Обработка нажатия на кнопку "Назад"
        backBtn.setOnClickListener {
            finish()  // Закрытие текущей активности и возврат к MainActivity
            // Убираем анимацию перехода
            overridePendingTransition(0, 0)
        }
    }
}