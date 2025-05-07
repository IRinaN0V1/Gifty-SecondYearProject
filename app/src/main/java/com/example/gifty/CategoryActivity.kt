package com.example.gifty

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Adapters.CategoriesAdapter
import com.example.gifty.Adapters.GiftsAdapter
import com.google.gson.JsonArray
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class CategoryActivity : AppCompatActivity() {
    @Inject
    lateinit var api: Api
    private lateinit var categoryName: String
    private lateinit var categoryAdapter: CategoriesAdapter
    private lateinit var recyclerView: RecyclerView
    private val selectedHobbies = mutableListOf<Int>()
    private val selectedHolidays = mutableListOf<Int>()
    private val selectedProfessions = mutableListOf<Int>()
    private val selectedHobbyNames = mutableListOf<String>()
    private val selectedHolidayNames = mutableListOf<String>()
    private val selectedProfessionNames = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        recyclerView = findViewById(R.id.categories_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val backBtn: ImageView = findViewById(R.id.backButton)
        // Получаем имя категории
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Категория"
        val title: TextView = findViewById(R.id.title)
        title.text = categoryName  // Устанавливаем заголовок

        restoreSelectedHobbies()
        restoreSelectedHolidays()
        restoreSelectedProfessions()

        loadCategories()

        // Обработка нажатия на кнопку "Назад"
        backBtn.setOnClickListener {
            saveSelectedCategoryNames()
            finish()  // Закрытие текущей активности и возврат к MainActivity
            // Убираем анимацию перехода
            overridePendingTransition(0, 0)
        }
    }

    private fun restoreSelectedHobbies() {
        val sharedPref = getSharedPreferences("categories_prefs", MODE_PRIVATE)
        val savedHobbies = sharedPref.getStringSet("selected_hobbies_ids", emptySet())
        for (id in savedHobbies.orEmpty()) {
            id.toIntOrNull()?.let {
                selectedHobbies.add(it)
            }
        }

        // Восстанавливаем названия
        val savedHobbyNames = sharedPref.getStringSet("selected_hobbies_names", emptySet())
        selectedHobbyNames.addAll(savedHobbyNames.orEmpty())
    }

    private fun restoreSelectedHolidays() {
        val sharedPref = getSharedPreferences("categories_prefs", MODE_PRIVATE)
        val savedHolidays = sharedPref.getStringSet("selected_holidays_ids", emptySet())
        for (id in savedHolidays.orEmpty()) {
            id.toIntOrNull()?.let {
                selectedHolidays.add(it)
            }
        }

        // Восстанавливаем названия
        val savedHolidayNames = sharedPref.getStringSet("selected_holidays_names", emptySet())
        selectedHolidayNames.addAll(savedHolidayNames.orEmpty())
    }

    private fun restoreSelectedProfessions() {
        val sharedPref = getSharedPreferences("categories_prefs", MODE_PRIVATE)
        val savedProfessions = sharedPref.getStringSet("selected_professions_ids", emptySet())
        for (id in savedProfessions.orEmpty()) {
            id.toIntOrNull()?.let {
                selectedProfessions.add(it)
            }
        }

        // Восстанавливаем названия
        val savedProfessionNames = sharedPref.getStringSet("selected_professions_names", emptySet())
        selectedProfessionNames.addAll(savedProfessionNames.orEmpty())
    }

    private fun saveSelectedCategoryNames() {
        val sharedPref = getSharedPreferences("categories_prefs", MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Сохраните названия хобби
        editor.putStringSet("selected_hobbies_names", HashSet(selectedHobbyNames))
        // Сохраните названия праздников
        editor.putStringSet("selected_holidays_names", HashSet(selectedHolidayNames))
        // Сохраните названия профессий
        editor.putStringSet("selected_professions_names", HashSet(selectedProfessionNames))

        editor.apply()
    }

    private fun loadCategories() {
        // Здесь вы можете реализовать логику для вызова соответствующего API
        // в зависимости от имени категории
        when (categoryName) {
            "Хобби" -> getHobbies()
            "Праздники" -> getHolidays()
            "Сферы деятельности" -> getProfessions()
        }
    }

    private fun getHobbies() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getHobbies()
                val jsonResponse = response.body()
                if (jsonResponse != null && !jsonResponse.get("error").asBoolean) {
                    val hobbiesArray = jsonResponse.getAsJsonArray("hobbies")
                    val hobbiesList = jsonConverter(hobbiesArray)
                    withContext(Dispatchers.Main) {
                        categoryAdapter = CategoriesAdapter(hobbiesList, selectedHobbies,
                            selectedHobbyNames, this@CategoryActivity, "Хобби")
                        recyclerView.adapter = categoryAdapter
                    }
                } else {
                    Toast.makeText(this@CategoryActivity, "Возникла ошибка. Пожалуйста, попробуйте еще раз.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("MyLog", "Exception occurred in ${javaClass.simpleName}: ${e.message}")
            }
        }
    }

    private fun jsonConverter(jsonArray: JsonArray): List<CategoryData> {
        val list = mutableListOf<CategoryData>()
        jsonArray.forEach { jsonElement ->
            val jsonObject = jsonElement.asJsonObject
            val listElement = CategoryData(
                id = jsonObject.get("id").asInt,
                name = jsonObject.get("name").asString
            )
            list.add(listElement)
        }
        return list
    }
    private fun getHolidays() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getHolidays()
                val jsonResponse = response.body()
                if (jsonResponse != null && !jsonResponse.get("error").asBoolean) {
                    val holidaysArray = jsonResponse.getAsJsonArray("holidays")
                    val holidaysList = jsonConverter(holidaysArray)
                    withContext(Dispatchers.Main) {
                        categoryAdapter = CategoriesAdapter(holidaysList, selectedHolidays, selectedHolidayNames, this@CategoryActivity, "Праздник")
                        recyclerView.adapter = categoryAdapter
                    }
                } else {
                    Toast.makeText(this@CategoryActivity, "Возникла ошибка. Пожалуйста, попробуйте еще раз.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("MyLog", "Exception occurred in ${javaClass.simpleName}: ${e.message}")
            }
        }
    }

    private fun getProfessions() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getProfessions()
                val jsonResponse = response.body()
                if (jsonResponse != null && !jsonResponse.get("error").asBoolean) {
                    val professionsArray = jsonResponse.getAsJsonArray("professions")
                    val professionsList = jsonConverter(professionsArray)
                    withContext(Dispatchers.Main) {
                        categoryAdapter = CategoriesAdapter(professionsList, selectedProfessions, selectedProfessionNames, this@CategoryActivity, "Профессия")
                        recyclerView.adapter = categoryAdapter
                    }
                } else {
                    Toast.makeText(this@CategoryActivity, "Возникла ошибка. Пожалуйста, попробуйте еще раз.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("MyLog", "Exception occurred in ${javaClass.simpleName}: ${e.message}")
            }
        }
    }
}