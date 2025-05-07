package com.example.gifty

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Adapters.CategoriesAdapter
import com.example.gifty.Adapters.GiftsAdapter
import com.example.gifty.Adapters.SelectedGiftsAdapter
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class FoundGiftsActivity : AppCompatActivity(), GiftsAdapter.OnGiftClickListener {
    @Inject
    lateinit var api: Api
    private var ageId: Int? = null
    private val giftsList = mutableListOf<Gift>()
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
        val selectedGenderId = intent.getIntExtra("selected_gender_id", -1)
        val age = intent.getIntExtra("age", -1)
        val hobbies = intent.getIntegerArrayListExtra("selected_hobbies")
        val professions = intent.getIntegerArrayListExtra("selected_professions")
        val holidays = intent.getIntegerArrayListExtra("selected_holidays")

        // Обработка нажатия на кнопку "Назад"
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()  // Закрытие текущей активности и возврат к MainActivity
            // Убираем анимацию перехода
            overridePendingTransition(0, 0)
        }

        // Преобразуем список хобби в строку типа
        val hobbiesString = hobbies?.joinToString(",") { it.toString() } ?: ""

        // Преобразуем список профессий в строку
        val professionsString = professions?.joinToString(",") { it.toString() } ?: ""

        // Преобразуем список праздников в строку
        val holidaysString = holidays?.joinToString(",") { it.toString() } ?: ""

        progressBar.visibility = View.VISIBLE

        Log.d("MyLog", "${selectedGenderId}")
        Log.d("MyLog", "${age}")
        Log.d("MyLog", "${hobbies}")
        Log.d("MyLog", "${professions}")
        Log.d("MyLog", "${holidays}")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Получаем ответ от API
                val response = api.getFoundGifts(selectedGenderId, age, hobbiesString, professionsString, holidaysString)
                val jsonResponse = response.body()
                if (jsonResponse != null) {
                    if (!jsonResponse.get("error").asBoolean){
                        val foundGifts = jsonResponse.get("foundgifts").asString
                        withContext(Dispatchers.Main) {
                            // Скрываем прогресс бар сразу
                            progressBar.visibility = View.GONE
                            if (foundGifts.isEmpty()) {
                                recyclerView.visibility = View.GONE
                                noResultsMessage.visibility = View.VISIBLE
                            } else {
                                val giftIds = foundGifts.split(",").map { it.trim() }

                                // Создаем новый список подарков
                                val giftsList = mutableListOf<Gift>()

                                // Выполняем запрос для каждого ID подарка
                                for (id in giftIds) {
                                    val giftResponse = api.getGiftById(id.toInt())
                                    val jsonResponse = giftResponse.body()
                                    if (jsonResponse != null) {
                                        val message = jsonResponse.get("message").asString
                                        if (!jsonResponse.get("error").asBoolean){
                                            val temp = jsonResponse.getAsJsonObject("gift")
                                            val gift = convertJsonToGift(temp)
                                            giftsList.add(gift)
                                        } else{
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(this@FoundGiftsActivity, message, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }
                                // Обновление UI с полученными данными
                                recyclerView.layoutManager = GridLayoutManager(this@FoundGiftsActivity, 2)
                                giftsAdapter = GiftsAdapter(giftsList, this@FoundGiftsActivity, this@FoundGiftsActivity)
                                recyclerView.adapter = giftsAdapter
                            }
                        }
                    } else{
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@FoundGiftsActivity, "Возникла ошибка. Пожалуйста, попробуйте еще раз.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MyLog", "Ошибка: ${e.message}")
                // Скрываем прогресс бар в случае ошибки
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun convertJsonToGift(jsonObject: JsonObject): Gift {
        val id = jsonObject.get("id").asInt
        val name = jsonObject.get("name").asString
        val image = jsonObject.get("image").asString
        val description = jsonObject.get("description").asString
        val gender = jsonObject.get("gender").asInt
        return Gift(id, name, image, description, gender)
    }

    override fun onGiftClick(gift: Gift) {
        val intent = Intent(this, GiftActivity::class.java)
        intent.putExtra("giftName", gift.name)
        intent.putExtra("giftDescription", gift.description)
        intent.putExtra("giftImage", gift.image)
        intent.putExtra("giftId", gift.id)
        startActivity(intent)
        this.overridePendingTransition(0, 0)
    }
}