package com.example.gifty.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Adapters.SelectedCategoriesAdapter
import com.example.gifty.Api
import com.example.gifty.R
import com.example.gifty.ViewModels.ParametersSearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ParametersSearchActivity : AppCompatActivity() {
    @Inject
    lateinit var api: Api
    private val viewModel: ParametersSearchViewModel by viewModels()

    private var ageCategoryId: Int = -1
    private var genderIds = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parameters_search)

        // Инициализация элементов интерфейса
        val hobbyButton: ImageView = findViewById(R.id.hobbyButton)
        val professionButton: ImageView = findViewById(R.id.professionButton)
        val holidayButton: ImageView = findViewById(R.id.holidayButton)
        val searchButton: ConstraintLayout = findViewById(R.id.searchButton)
        val ageEditText: EditText = findViewById(R.id.age)
        val genderSpinner: Spinner = findViewById(R.id.spinner_gender)

        // Установка адаптера для спиннера с категориями пола
        val genderOptions = resources.getStringArray(R.array.gender_options)
        genderSpinner.adapter = ArrayAdapter(this, R.layout.gender_dropdown_item, genderOptions)

        // Обработка нажатия кнопки "Назад"
        val backBtn: ImageView = findViewById(R.id.backButton)
        backBtn.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        // Переход в другие категории при нажатии соответствующих кнопок
        hobbyButton.setOnClickListener { navigateToCategory("Хобби") }
        professionButton.setOnClickListener { navigateToCategory("Сферы деятельности") }
        holidayButton.setOnClickListener { navigateToCategory("Праздники") }

        // Обработка нажатия кнопки "Поиск"
        searchButton.setOnClickListener {
            performSearch(genderSpinner, ageEditText)
        }

        // Настройка RecyclerView при создании активности
        setupRecyclerViews()
    }

    // Метод для перехода в выбранную категорию
    private fun navigateToCategory(categoryName: String) {
        val intent = Intent(this, CategoryActivity::class.java)
        intent.putExtra("CATEGORY_NAME", categoryName)
        startActivity(intent)
    }

    // Метод для выполнения поиска на основе введенных данных
    private fun performSearch(genderSpinner: Spinner, ageEditText: EditText) {
        // Очистка списка выбранного пола
        genderIds.clear()

        // Определение выбранного пола
        when (genderSpinner.selectedItemPosition) {
            2 -> {
                genderIds.add(2) // Женский
                genderIds.add(3) // Универсальный
            }
            1 -> {
                genderIds.add(1) // Мужской
                genderIds.add(3)// Универсальный
            }
            0 -> {
                genderIds.add(1) // Мужской
                genderIds.add(2) // Женский
                genderIds.add(3) // Универсальный
            }
        }

        // Получение возраста
        val ageInput = ageEditText.text.toString().trim()
        var age = -1 // По умолчанию устанавливаем -1, если поле пустое

        if (ageInput.isNotBlank()) {
            age = ageInput.toIntOrNull() ?: -1

            // Проверяем, что полученный возраст корректный
            if (age < 0 || age > 120) {
                Toast.makeText(this, "Некорректный возраст", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Выполнение сетевого запроса для получения категории возраста
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (age != -1) { // Проверяем, что возраст указан
                    val response = api.getAgeCategoryByAge(age)
                    val jsonResponse = response.body()

                    if (jsonResponse != null && !jsonResponse.get("error").asBoolean) {
                        // Сохраняем идентификатор категории возраста
                        ageCategoryId = jsonResponse.get("agecategoryid").asInt
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ParametersSearchActivity, "Возникла ошибка обработки возраста", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MyLog", "Exception occurred in ${javaClass.simpleName}: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ParametersSearchActivity, "Ошибка в сетевом запросе", Toast.LENGTH_LONG).show()
                }
            }

            // Переход на новую активность после получения идентификатора категории возраста
            withContext(Dispatchers.Main) {
                startFoundGiftsActivity(age)
            }
        }
    }

    // Метод для запуска активности с найденными подарками
    private fun startFoundGiftsActivity(inputAge: Int) {
        val intent = Intent(this, FoundGiftsActivity::class.java).apply {
            putIntegerArrayListExtra("selected_gender_ids", ArrayList(genderIds))
            putExtra("age", if (inputAge == -1) -1 else ageCategoryId) // Передаем -1 если поле было пустым
            putIntegerArrayListExtra("selected_hobbies", ArrayList(getSelectedHobbies()))
            putIntegerArrayListExtra("selected_professions", ArrayList(getSelectedProfessions()))
            putIntegerArrayListExtra("selected_holidays", ArrayList(getSelectedHolidays()))
        }

        startActivity(intent) // Запускаем новую активность
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerViews()
    }

    // Метод для настройки RecyclerView всех категорий
    private fun setupRecyclerViews() {
        setupRecyclerView(R.id.hobbies_list, "selected_hobbies_names")
        setupRecyclerView(R.id.holidays_list, "selected_holidays_names")
        setupRecyclerView(R.id.professions_list, "selected_professions_names")
    }

    // Метод для настройки отдельного RecyclerView
    private fun setupRecyclerView(recyclerViewId: Int, key: String) {
        val horizontalRecyclerView = findViewById<RecyclerView>(recyclerViewId)
        // Горизонтальный список
        horizontalRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        val sharedPref = getSharedPreferences("categories_prefs", MODE_PRIVATE)
        // Получаем сохраненные элементы
        val savedItems = sharedPref.getStringSet(key, emptySet())!!.toMutableList()

        // Создаем адаптер и устанавливаем его в RecyclerView
        val adapter = SelectedCategoriesAdapter(savedItems, this)
        horizontalRecyclerView.adapter = adapter
    }

    // Получение выбранных хобби, профессий и праздников из ViewModel
    private fun getSelectedHobbies(): List<Int> = viewModel.getSelectedIds("selected_hobbies_ids")
    private fun getSelectedProfessions(): List<Int> = viewModel.getSelectedIds("selected_professions_ids")
    private fun getSelectedHolidays(): List<Int> = viewModel.getSelectedIds("selected_holidays_ids")
}