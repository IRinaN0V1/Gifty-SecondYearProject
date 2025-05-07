package com.example.gifty

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class EventActivity : AppCompatActivity() {
    @Inject
    lateinit var api: Api
    private lateinit var eventName: TextView
    private lateinit var eventDate: TextView
    private lateinit var eventDescription: TextView
    private lateinit var reminderTime: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_event)

        // Получаем переданные данные из Intent
        val id = intent.getIntExtra("event_id", -1)

        eventName = findViewById(R.id.eventName)
        eventDate = findViewById(R.id.eventDate)
        eventDescription = findViewById(R.id.eventDescription)
        reminderTime = findViewById(R.id.reminderTime)
        val editBtn: ConstraintLayout = findViewById(R.id.editEventButton)
        val backBtn: ImageView = findViewById(R.id.backButton)
        val deleteBtn: ImageView = findViewById(R.id.deleteEventButton)

        // Обработка нажатия на кнопку "Назад"
        backBtn.setOnClickListener {
            finish()  // Закрытие текущей активности и возврат к MainActivity
            // Убираем анимацию перехода
            overridePendingTransition(0, 0)
        }

        editBtn.setOnClickListener{
            val intent = Intent(this@EventActivity, NewEventActivity::class.java)
            intent.putExtra("event_id", id)
            intent.putExtra("event_name", eventName.text.toString())
            intent.putExtra("event_date", eventDate.text.toString())
            intent.putExtra("event_description", eventDescription.text.toString())
            intent.putExtra("event_reminder_time", reminderTime.text.toString())
            Log.d("MyLog", "${reminderTime.text}")
            startActivity(intent)
            this.overridePendingTransition(0, 0)
        }

        deleteBtn.setOnClickListener{
            val sp = getSharedPreferences("PC", Context.MODE_PRIVATE)
            // Создание AlertDialog для подтверждения удаления
            AlertDialog.Builder(this@EventActivity).apply {
                setTitle("Подтверждение удаления")
                setMessage("Вы уверены, что хотите удалить это событие?")

                setPositiveButton("Да") { _, _ ->
                    // Логика удаления события
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = api.deleteEvent(id)
                            val jsonResponse = response.body()
                            if (jsonResponse != null) {
                                val message = jsonResponse.get("message").asString
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@EventActivity, message, Toast.LENGTH_LONG).show()
                                }
                                finish()
                                overridePendingTransition(0, 0)
                            }

                        } catch (e: Exception) {
                            Log.e("MyLog", e.message ?: "Unknown exception")
                        }
                    }
                }

                setNegativeButton("Нет") { dialog, _ ->
                    dialog.dismiss() // Закрыть диалог при выборе "Нет"
                }
                create().show() // Показать диалог
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Обновляем данные при каждом возвращении на эту активность
        loadEventDetails(intent.getIntExtra("event_id", -1))
    }

    private fun parseReminderTime(reminderTime: String): String? {
        return try {
            // Определяем формат входных данных
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            // Парсим строку в объект Date
            val date: Date = inputFormat.parse(reminderTime) ?: return null

            // Определяем формат для отображения
            val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            // Форматируем дату
            outputFormat.format(date)
        } catch (e: Exception) {
            // Логирование ошибки
            Log.e("MyLog", "Ошибка разбора времени напоминания: $reminderTime", e)
            null // В случае ошибки вернуть null
        }
    }

    private fun parseDate(eventDate: String): String? {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date: Date = dateFormat.parse(eventDate) ?: return null

            val calendar = Calendar.getInstance()
            calendar.time = date

            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1 // Месяцы начинаются с 0
            val year = calendar.get(Calendar.YEAR)

            String.format("%02d.%02d.%d", day, month, year)
        } catch (e: Exception) {
            null
        }
    }

    private fun loadEventDetails(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getEventById(id)
                val jsonResponse = response.body()
                if (jsonResponse != null) {
                    val message = jsonResponse.get("message").asString
                    if (!jsonResponse.get("error").asBoolean){
                        val event = jsonResponse.getAsJsonObject("event")
                        val eventDate = event.get("event_date").asString
                        val eventName = event.get("name").asString
                        val eventReminderTime = event.get("reminder_time").asString
                        val eventDescription = event.get("description").asString
                        withContext(Dispatchers.Main) {
                            val formattedReminderTime = parseReminderTime(eventReminderTime)
                            // Обновите ваши текстовые поля здесь
                            findViewById<TextView>(R.id.eventName).text = eventName
                            findViewById<TextView>(R.id.eventDate).text = parseDate(eventDate)
                            findViewById<TextView>(R.id.eventDescription).text = eventDescription
                            findViewById<TextView>(R.id.reminderTime).text = formattedReminderTime
                        }
                    } else{
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EventActivity, message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MyLog", "Ошибка загрузки события: ${e.message}")
            }
        }
    }
}