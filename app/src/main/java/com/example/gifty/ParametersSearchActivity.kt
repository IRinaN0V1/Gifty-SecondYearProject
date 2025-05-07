package com.example.gifty
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Adapters.SelectedCategoriesAdapter
import com.example.gifty.Adapters.SelectedGiftsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class ParametersSearchActivity : AppCompatActivity() {
    @Inject
    lateinit var api: Api
    private var ageId: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_parameters_search)
        val hobbyButton: ImageView = findViewById(R.id.hobbyButton)
        val professionButton: ImageView = findViewById(R.id.professionButton)
        val holidayButton: ImageView = findViewById(R.id.holidayButton)
        val searchButton: ConstraintLayout = findViewById(R.id.searchButton) // Кнопка поиска
        val ageEditText: EditText = findViewById(R.id.age)
        // Связываем spinner с ресурсом
        val genderSpinner = findViewById<Spinner>(R.id.spinner_gender)

        // Установка ArrayAdapter с использованием ресурса массива
        val genderOptions = resources.getStringArray(R.array.gender_options)
        val adapter = ArrayAdapter(this, R.layout.gender_dropdown_item, genderOptions)
        genderSpinner.adapter = adapter

        val backBtn: ImageView = findViewById(R.id.backButton)


        backBtn.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }
        hobbyButton.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("CATEGORY_NAME", "Хобби")  // Передаем название категории
            startActivity(intent)
            this.overridePendingTransition(0, 0)
        }

        professionButton.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("CATEGORY_NAME", "Сферы деятельности")  // Передаем название категории
            startActivity(intent)
            this.overridePendingTransition(0, 0)
        }

        holidayButton.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("CATEGORY_NAME", "Праздники")  // Передаем название категории
            startActivity(intent)
            this.overridePendingTransition(0, 0)
        }
        searchButton.setOnClickListener {
            performSearch(genderSpinner, ageEditText)
        }
    }

    private fun performSearch(genderSpinner: Spinner, ageEditText: EditText) {
        // Получаем данные
        var gender = 3
        if (genderSpinner.selectedItemPosition == 1) {
            gender = 1
        } else if (genderSpinner.selectedItemPosition == 2) {
            gender = 2
        }
        val age = ageEditText.text.toString().ifEmpty { "-1" }.toInt()
        if (age != -1){
            runBlocking {
                try {
                    val job = launch(Dispatchers.IO) {
                        val response = api.getAgeCategoryByAge(age)
                        val jsonResponse = response.body()
                        if (jsonResponse != null) {
                            if (!jsonResponse.get("error").asBoolean){
                                ageId = jsonResponse.get("agecategoryid").asInt
                            }
                        }
                    }
                    // Ожидание завершения выполнения запроса
                    job.join()
                } catch (e: Exception) {
                    Log.e("MyLog", "Ошибка: ${e.message}")
                }
            }
        }
        else{
            ageId = age
        }

        // Получаем выбранные хобби
        val hobbies = getSelectedHobbies()
        val professions = getSelectedProfessions()
        val holidays = getSelectedHolidays()

        // Создаем Intent для перехода на следующую активность
        val intent = Intent(this, FoundGiftsActivity::class.java)
        intent.putExtra("selected_gender_id", gender)
        Log.d("MyLog", "Полученный ageId 2: $ageId")
        intent.putExtra("age", ageId)
        intent.putIntegerArrayListExtra("selected_hobbies", ArrayList(hobbies))
        intent.putIntegerArrayListExtra("selected_professions", ArrayList(professions))
        intent.putIntegerArrayListExtra("selected_holidays", ArrayList(holidays))

        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        setupHobbiesRecyclerView()
        setupHolidaysRecyclerView()
        setupProfessionsRecyclerView()
    }

    private fun setupHobbiesRecyclerView() {
        val horizontalRecyclerView = findViewById<RecyclerView>(R.id.hobbies_list)
        horizontalRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        val sharedPref = getSharedPreferences("categories_prefs", MODE_PRIVATE)
        val savedHobbies = sharedPref.getStringSet("selected_hobbies_names", emptySet())!!.toMutableList()

        // Создаем адаптер и устанавливаем его
        val adapter = SelectedCategoriesAdapter(savedHobbies, this)
        horizontalRecyclerView.adapter = adapter
    }
    private fun setupHolidaysRecyclerView() {
        val horizontalRecyclerView = findViewById<RecyclerView>(R.id.holidays_list)
        horizontalRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        val sharedPref = getSharedPreferences("categories_prefs", MODE_PRIVATE)
        val savedHolidays = sharedPref.getStringSet("selected_holidays_names", emptySet())!!.toMutableList()

        // Создаем адаптер и устанавливаем его
        val adapter = SelectedCategoriesAdapter(savedHolidays, this)
        horizontalRecyclerView.adapter = adapter
    }
    private fun setupProfessionsRecyclerView() {
        val horizontalRecyclerView = findViewById<RecyclerView>(R.id.professions_list)
        horizontalRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        val sharedPref = getSharedPreferences("categories_prefs", MODE_PRIVATE)
        val savedProfessions = sharedPref.getStringSet("selected_professions_names", emptySet())!!.toMutableList()

        // Создаем адаптер и устанавливаем его
        val adapter = SelectedCategoriesAdapter(savedProfessions, this)
        horizontalRecyclerView.adapter = adapter
    }

    private fun getSelectedHobbies(): List<Int> {
        val sharedPref = getSharedPreferences("categories_prefs", MODE_PRIVATE)
        val savedHobbies = sharedPref.getStringSet("selected_hobbies_ids", emptySet())?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        return savedHobbies
    }

    private fun getSelectedProfessions(): List<Int> {
        val sharedPref = getSharedPreferences("categories_prefs", MODE_PRIVATE)
        val savedProfessions = sharedPref.getStringSet("selected_professions_ids", emptySet())?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        return savedProfessions
    }

    private fun getSelectedHolidays(): List<Int> {
        val sharedPref = getSharedPreferences("categories_prefs", MODE_PRIVATE)
        val savedHolidays = sharedPref.getStringSet("selected_holidays_ids", emptySet())?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        return savedHolidays
    }

}