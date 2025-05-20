package com.example.gifty.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Adapters.GiftsAdapter
import com.example.gifty.Api
import com.example.gifty.Data.Gift
import com.example.gifty.R
import com.example.gifty.ViewModels.FoundGiftsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FoundGiftsActivity : AppCompatActivity(), GiftsAdapter.OnGiftClickListener {

    @Inject
    lateinit var api: Api
    private val viewModel: FoundGiftsViewModel by viewModels()
    private lateinit var giftsAdapter: GiftsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noResultsMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_found_gifts)

        recyclerView = findViewById(R.id.gifts_view)
        progressBar = findViewById(R.id.progressBar)
        noResultsMessage = findViewById(R.id.no_results_message)

        // Получение данных из Intent
        val selectedGenderId = intent.getIntegerArrayListExtra("selected_gender_ids")?.joinToString(",") { it.toString() } ?: ""
        val age = intent.getIntExtra("age", -1)
        val hobbies = intent.getIntegerArrayListExtra("selected_hobbies")?.joinToString(",") { it.toString() } ?: ""
        val professions = intent.getIntegerArrayListExtra("selected_professions")?.joinToString(",") { it.toString() } ?: ""
        val holidays = intent.getIntegerArrayListExtra("selected_holidays")?.joinToString(",") { it.toString() } ?: ""

        // Обработка нажатия на кнопку "Назад"
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        // Настройка наблюдателей
        setupObservers()

        // Загружаем подарки подходящие под парметры
        viewModel.fetchFoundGifts(selectedGenderId, age, hobbies, professions, holidays)
    }

    // Настройка наблюдателей
    private fun setupObservers() {
        viewModel.loading.observe(this) { isLoading ->
            // Показываем или скрываем прогресс-бар
            showProgressBar(isLoading)
            // Скрываем recyclerView при загрузке
            recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
            if (!isLoading) {
                noResultsMessage.visibility = View.GONE
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            // Если есть сообщение об ошибке, показываем уведомление пользователю
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.giftsList.observe(this) { gifts ->
            // Проверяем, есть ли подарки в списке
            if (gifts.isNullOrEmpty()) {
                recyclerView.visibility = View.GONE
                // Показываем сообщение об отсутствии результатов
                noResultsMessage.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                // Скрываем сообщение об отсутствии результатов
                noResultsMessage.visibility = View.GONE
                // Выводим подарки на экран
                displayGifts(gifts)
            }
        }
    }

    // Метод для отображения списка подарков
    private fun displayGifts(gifts: List<Gift>) {
        // Инициализация адаптера с полученными подарками
        giftsAdapter = GiftsAdapter(gifts, this, this)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = giftsAdapter
    }

    // Метод для отображения/скрытия прогресс-бара
    private fun showProgressBar(isVisible: Boolean) {
        progressBar.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    // Обработка клика на подарок
    override fun onGiftClick(gift: Gift) {
        // Создаем Intent для перехода на экран подарка
        val intent = Intent(this, GiftActivity::class.java)
        intent.putExtra("giftName", gift.name)
        intent.putExtra("giftDescription", gift.description)
        intent.putExtra("giftImage", gift.image)
        intent.putExtra("giftId", gift.id)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}