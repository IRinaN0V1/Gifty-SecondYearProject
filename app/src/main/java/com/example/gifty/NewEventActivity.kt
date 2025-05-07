package com.example.gifty

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class NewEventActivity : AppCompatActivity() {
    @Inject
    lateinit var api: Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_event)
        var selectedDate: TextView = findViewById(R.id.selectedDate)
        var eventName: EditText = findViewById(R.id.eventName)
        var eventDescription: EditText = findViewById(R.id.eventDescription)
        var reminderTime: EditText = findViewById(R.id.reminderTime)
        val newEventButton: ConstraintLayout = findViewById(R.id.newEventButton)
        var buttonText: TextView = findViewById(R.id.newEventButtonTextView)
        var title: TextView = findViewById(R.id.textView)


        // Получаем из интента
        val receivedDate = intent.getStringExtra("SELECTED_DATE") ?: ""
        val id = intent.getIntExtra("event_id", -1)
        val name = intent.getStringExtra("event_name")
        val date = intent.getStringExtra("event_date")
        val description = intent.getStringExtra("event_description")
        val reminderEvent = intent.getStringExtra("event_reminder_time")

        val calendarBtn = findViewById<ImageView>(R.id.calendarButton)

        selectedDate.text = receivedDate
        Log.d("MyLog", "НАпоминание ${reminderEvent}")
        // Получаем текущее время
        // Обработка выбранной даты
        val calendar = Calendar.getInstance()
        if (receivedDate.isNotEmpty()) {
            // Преобразуем строку в дату
            val parts = receivedDate.split(".")
            Log.d("MyLog", "${parts}")
            if (parts.size == 3) {
                calendar.set(Calendar.YEAR, parts[2].toInt())
                calendar.set(Calendar.MONTH, parts[1].toInt() - 1) // Месяцы начинаются с 0
                calendar.set(Calendar.DAY_OF_MONTH, parts[0].toInt())
            }
        }

        fun convertReminderTimeToDatabaseFormat(reminderTime: String): String? {
            return try {
                val inputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val date = inputFormat.parse(reminderTime)
                val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                outputFormat.format(date!!)
            } catch (e: Exception) {
                Log.e("MyLog", "Ошибка форматирования даты: $reminderTime", e)
                null
            }
        }
        var fullReminderTimeForDB = ""
        if (id != -1) {
            // При загрузке события преобразуем формат времени
            val formattedReminderTime = convertReminderTimeToDatabaseFormat(reminderEvent ?: "")
            reminderTime.setText(formattedReminderTime)
            fullReminderTimeForDB = formattedReminderTime ?: ""
        } else {
            // Устанавливаем значение по умолчанию в формате "дд.мм.гггг чч:мм"
            val userFriendlyFormat = String.format("%02d.%02d.%d %02d:%02d",
                calendar.get(Calendar.DAY_OF_MONTH) -1,
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR),
                12,
                0)
            reminderTime.setText(userFriendlyFormat)

            // Преобразуем в формат для БД
            fullReminderTimeForDB = convertReminderTimeToDatabaseFormat(userFriendlyFormat) ?: ""
        }



        calendarBtn.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // После выбора даты показываем TimePickerDialog
                val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                    // Формат для пользователя
                    val userFriendlyFormat = String.format("%02d.%02d.%d %02d:%02d",
                        selectedDay, selectedMonth + 1, selectedYear, selectedHour, selectedMinute)
                    reminderTime.setText(userFriendlyFormat)

                    // Формат для сохранения в БД
                    fullReminderTimeForDB = String.format("%d-%02d-%02d %02d:%02d",
                        selectedYear, selectedMonth + 1, selectedDay, selectedHour, selectedMinute) + ":00"
                }, 12, 0, true) // по умолчанию 12:00
                timePickerDialog.show()
            }, year, month, dayOfMonth)
            datePickerDialog.show()
        }
        Log.e("MyLog", "Exception occurred: ${reminderTime.text}")

        Log.e("MyLog", "Exception occurred: ${fullReminderTimeForDB}")

        var  sp = getSharedPreferences("PC", Context.MODE_PRIVATE)

        if (id != -1) {
            title.text = "Изменение события"
            eventName.setText(name)
            eventDescription.setText(description)
            selectedDate.setText(date)
            reminderTime.setText(reminderEvent)
            buttonText.text = "Сохранить"
            newEventButton.setOnClickListener{
                if (eventName.text.isEmpty() || eventName.text.toString() == " "){
                    Toast.makeText(this, "Укажите название события", Toast.LENGTH_LONG).show()
                }
                else{
                    CoroutineScope(Dispatchers.IO).launch {
                        try{
                            val response = api.updateEvent(id, eventName.text.toString(), fullReminderTimeForDB, eventDescription.text.toString())
                            val jsonResponse = response.body()
                            if (jsonResponse != null) {
                                val message = jsonResponse.get("message").asString
                                if (!jsonResponse.get("error").asBoolean){
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@NewEventActivity, message, Toast.LENGTH_LONG).show()
                                        finish() // Закрытие активности
                                        overridePendingTransition(0, 0)
                                    }
                                } else{
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@NewEventActivity, message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }catch (e: Exception) {
                            Log.e("MyLog", "Exception occurred: ${e.message}")
                        }

                    }
                }
            }
        } else{
            newEventButton.setOnClickListener{
                if (eventName.text.isEmpty() || eventName.text.toString() == " "){
                    Toast.makeText(this, "Укажите название события", Toast.LENGTH_LONG).show()
                }
                else{
                    val formattedSelectedDate = formatSelectedDate(selectedDate.text.toString())
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = api.createEvent(sp.getInt("UserId", -1), eventName.text.toString(), fullReminderTimeForDB, eventDescription.text.toString(), formattedSelectedDate)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@NewEventActivity, "Событие создано", Toast.LENGTH_LONG).show()
                                // Планируем напоминание
                                val reminderCal = Calendar.getInstance()
                                val uniqueEventID = System.currentTimeMillis().toInt()
                                scheduleEventReminder(applicationContext, uniqueEventID.toLong(), eventName.text.toString(), selectedDate.text.toString(), fullReminderTimeForDB)
                            }
                            finish()  // Закрытие текущей активности и возврат к MainActivity
                            // Убираем анимацию перехода
                            overridePendingTransition(0, 0)
                        } catch (e: Exception) {
                            Log.e("MyLog", "Exception occurred: ${e.message}")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@NewEventActivity, "Ошибка при создании события", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

            }
        }

        // Обработка нажатия на кнопку "Назад"
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()  // Закрытие текущей активности и возврат к MainActivity
            // Убираем анимацию перехода
            overridePendingTransition(0, 0)
        }
    }
    fun scheduleEventReminder(context: Context, eventID: Long, eventTitle: String, eventDate: String, reminderTimeStr: String) {
        // Парсим строку даты-времени
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val reminderDate = sdf.parse(reminderTimeStr)
        val reminderCal = Calendar.getInstance()
        reminderCal.time = reminderDate
        Log.d("MyLog", "${sdf}, ${reminderDate}, ${reminderCal.time}")

        // Создаем PendingIntent для запуска нашего Broadcast Receiver
        val intent = Intent(context, EventNotificationReceiver::class.java).apply {
            putExtra("event_title", eventTitle)
            putExtra("event_date", eventDate)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, eventID.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Используем AlarmManager для планирования уведомления
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31+
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderCal.timeInMillis, pendingIntent)
            } else {
                askToEnableExactAlarms(context)
            }
        } else {
            // Реализация для API < 31
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                reminderCal.timeInMillis,
                AlarmManager.INTERVAL_DAY, // пример интервала повторения
                pendingIntent
            )
        }
    }

    private fun formatSelectedDate(dateString: String): String {
        return try {
            // Например, если dateString в формате дд.мм.гггг
            val parts = dateString.split(".")
            if (parts.size == 3) {
                val day = parts[0]
                val month = parts[1]
                val year = parts[2]
                // Возвращаем в формате гггг-мм-дд
                "$year-$month-$day"
            } else {
                // Возвращаем оригинальную строку, если формат неправильный
                dateString
            }
        } catch (e: Exception) {
            Log.e("MyLog", "Ошибка форматирования даты: ${e.message}")
            dateString // Возвращаем оригинал в случае ошибки
        }
    }

    // Метод для открытия экрана разрешений точной установки будильников
    private fun askToEnableExactAlarms(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            // Обработка для старых версий Android
            Toast.makeText(context, "Установка точного будильника невозможна", Toast.LENGTH_SHORT).show()
        }
    }
}