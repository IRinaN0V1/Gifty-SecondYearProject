package com.example.gifty.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.example.gifty.R

class GiftActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gift)

        // Получение данных из Intent
        val name = intent.getStringExtra("giftName")
        val description = intent.getStringExtra("giftDescription")
        val imageUrl = intent.getStringExtra("giftImage")
        val id = intent.getIntExtra("giftId", -1)

        val giftName: TextView = findViewById(R.id.giftName)
        val giftDescription: TextView = findViewById(R.id.description)
        val image: ImageView = findViewById(R.id.image)
        val addGiftToFormBtn: ConstraintLayout = findViewById(R.id.addGiftToForm)
        val backBtn: ImageView = findViewById(R.id.backButton)

        // Установка названия и описания подарка
        giftName.text = formatName(name)
        giftDescription.text = description

        // Загрузка изображения
        Glide.with(this).load(imageUrl).into(image)

        // Обработка нажатия на кнопку добавления подарка в анкету
        addGiftToFormBtn.setOnClickListener {
            // Создаем Intent для перехода к ChoseFormActivity
            val intent = Intent(this, ChoseFormActivity::class.java)
            intent.putExtra("giftId", id)
            startActivity(intent)
            this.overridePendingTransition(0, 0)
        }

        // Обработка нажатия на кнопку "Назад"
        backBtn.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }
    }

    // Метод для форматирования названия подарка
    private fun formatName(name: String?): String {
        val maxLength = 15 // Максимальная длина названия
        return if (!name.isNullOrEmpty() && name.length > maxLength) {
            // Обрезаем имя и добавляем троеточие в конце
            name.substring(0, maxLength) + "..."
        } else {
            // Возвращаем пустую строку, если имя null
            name ?: ""
        }
    }
}