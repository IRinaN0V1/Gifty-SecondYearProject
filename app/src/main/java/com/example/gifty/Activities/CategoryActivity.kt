package com.example.gifty.Activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Adapters.CategoriesAdapter
import com.example.gifty.Api
import com.example.gifty.R
import com.example.gifty.ViewModels.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CategoryActivity : AppCompatActivity() {
    @Inject
    lateinit var api: Api
    private val viewModel: CategoryViewModel by viewModels()
    private lateinit var categoryName: String
    private lateinit var categoryAdapter: CategoriesAdapter
    private lateinit var recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        // Инициализация RecyclerView, который необходим для отображения списка категорий
        recyclerView = findViewById(R.id.categories_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val backBtn: ImageView = findViewById(R.id.backButton)

        // Получаем имя категории из интента
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Категория"
        // Устанавливаем название категории как заголовок страницы
        val title: TextView = findViewById(R.id.title)
        title.text = categoryName

        // Определяем имя для загрузки категорий на основании имени полученной категории
        var name = ""
        if (categoryName == "Хобби"){
            name = "hobbies"
        } else if (categoryName == "Праздники"){
            name = "holidays"
        } else{
            name = "professions"
        }

        // Восстанавливаем выбранные значения через ViewModel
        viewModel.restoreSelectedValues(name)

        // Загружаем категории через ViewModel
        viewModel.loadCategories(name)

        // Наблюдаем за изменениями в категориях
        observeViewModel()

        // Обработка нажатия на кнопку "Назад"
        backBtn.setOnClickListener {
            // Сохраняем выбранные значения
            viewModel.saveSelectedValues()
            finish()
            overridePendingTransition(0, 0)
        }
    }

    private fun observeViewModel() {
        // Наблюдаем за изменениями в списке категорий
        viewModel.categories.observe(this) { categories ->
            // Создаем адаптер для категорий и устанавливаем его в RecyclerView
            categoryAdapter = CategoriesAdapter(
                categories,
                viewModel.selectedIds,
                viewModel.selectedNames,
                this,
                viewModel.currentCategory
            )
            // Устанавливаем адаптер в RecyclerView
            recyclerView.adapter = categoryAdapter
        }

        // Наблюдаем за ошибками и отображаем соответствующее сообщение
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }
}