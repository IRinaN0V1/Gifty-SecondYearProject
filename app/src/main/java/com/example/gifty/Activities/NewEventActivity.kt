package com.example.gifty.Activities

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
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.gifty.Api
import com.example.gifty.Adapters.EventNotificationReceiver
import com.example.gifty.R
import com.example.gifty.ViewModels.NewEventViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class NewEventActivity : AppCompatActivity() {
    @Inject
    lateinit var api: Api

    private val viewModel: NewEventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_event)

        // Инициализация элементов интерфейса
        var selectedDate: TextView = findViewById(R.id.selectedDate)
        var eventName: EditText = findViewById(R.id.eventName)
        var eventDescription: EditText = findViewById(R.id.eventDescription)
        var reminderTimeText: EditText = findViewById(R.id.reminderTime)
        val newEventButton: ConstraintLayout = findViewById(R.id.newEventButton)
        var buttonText: TextView = findViewById(R.id.newEventButtonTextView)
        var title: TextView = findViewById(R.id.textView)

        // Получаем данные из Intent
        val receivedDate = intent.getStringExtra("SELECTED_DATE") ?: ""
        val id = intent.getIntExtra("event_id", -1)
        val name = intent.getStringExtra("event_name")
        val date = intent.getStringExtra("event_date")
        val description = intent.getStringExtra("event_description")
        val reminderTime = intent.getStringExtra("event_reminder_time")
        val calendarBtn = findViewById<ImageView>(R.id.calendarButton)

        // Устанавливаем выбранную дату
        selectedDate.text = receivedDate

        val calendar = Calendar.getInstance()
        if (receivedDate.isNotEmpty()) {
            // Парсим получаемую дату и записываем в календарь
            val parts = receivedDate.split(".")
            if (parts.size == 3) {
                calendar.set(Calendar.YEAR, parts[2].toInt())
                calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
                calendar.set(Calendar.DAY_OF_MONTH, parts[0].toInt())
            }
        }

        // Устанавливаем начальное значение времени напоминания
        if (id != -1) {
            reminderTimeText.setText(reminderTime)
        } else {
            // Формируем дефолтное время напоминания
            val userFriendlyFormat = String.format("%02d.%02d.%d %02d:%02d",
                calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR), 12, 0)
            reminderTimeText.setText(userFriendlyFormat)
        }

        // При нажатии на дату
        calendarBtn.setOnClickListener {
            // Получаем дату
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            // Диалог выбора даты
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Создание диалога выбора времени после выбора даты
                val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                    // Сохраняем введённое время и дату
                    val userFriendlyFormat = String.format("%02d.%02d.%d %02d:%02d",
                        selectedDay, selectedMonth + 1, selectedYear, selectedHour, selectedMinute)
                    reminderTimeText.setText(userFriendlyFormat)
                }, 12, 0, true)
                timePickerDialog.show()
            }, year, month, dayOfMonth)
            datePickerDialog.show()
        }

        val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getInt("user_id", -1)

        // Для существующего события меняем заголовок и содержимое полей
        if (id != -1) {
            title.text = "Изменение события"
            eventName.setText(name)
            eventDescription.setText(description)
            selectedDate.setText(date)
            buttonText.text = "Сохранить"
        }

        // Нажатие на кнопку создания нового события
        newEventButton.setOnClickListener {
            // Получаем дату и время напоминания
            val reminderTimeStr = reminderTimeText.text.toString()
            val eventDateStr = selectedDate.text.toString()

            // Проверка, что у события есть название
            if (eventName.text.isEmpty() || eventName.text.toString() == " ") {
                Toast.makeText(this, "Укажите название события", Toast.LENGTH_LONG).show()
            } else if (!isReminderDateValid(reminderTimeStr, eventDateStr)) {
                // Сообщаем, если дата напоминания больше самой даты события
                Toast.makeText(this, "Дата напоминания не может быть позже даты события!", Toast.LENGTH_LONG).show()
            } else {
                // Создаем или обновляем событие
                if (id != -1) {
                    viewModel.updateEvent(id, eventName.text.toString(), reminderTimeStr, eventDescription.text.toString())
                } else {
                    viewModel.createEvent(userId, eventName.text.toString(), reminderTimeStr, eventDescription.text.toString(), selectedDate.text.toString())
                }
            }
        }

        // Нажатие на кнопку назад
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        // Наблюдаем за созданием события
        viewModel.createResult.observe(this) { result ->
            if (result) {
                Toast.makeText(this, "Событие создано", Toast.LENGTH_SHORT).show()
                val uniqueEventID = System.currentTimeMillis().toInt()
                scheduleEventReminder(applicationContext, uniqueEventID.toLong(), eventName.text.toString(), selectedDate.text.toString(), reminderTimeText.text.toString())
                finish()
                overridePendingTransition(0, 0)
            } else {
                Toast.makeText(this, "Для сохранения необходимо изменить данные", Toast.LENGTH_SHORT).show()
            }
        }

        // Наблюдаем за изменением события
        viewModel.changeEventResult.observe(this) { result ->
            if (result) {
                Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show()
                finish()
                overridePendingTransition(0, 0)
            } else {
                Toast.makeText(this, "Возникла ошибка. Пожалуйста, попробуйте еще раз.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun isReminderDateValid(reminderTimeStr: String, eventDateStr: String): Boolean {
        // Разбираем строку даты напоминания
        val reminderDate = reminderTimeStr.split(" ")[0]
        val eventParts = eventDateStr.split(".")
        val reminderParts = reminderDate.split(".")

        // Конвертируем части даты в целые числа
        val eventDay = eventParts[0].toInt()
        val eventMonth = eventParts[1].toInt()
        val eventYear = eventParts[2].toInt()

        val reminderDay = reminderParts[0].toInt()
        val reminderMonth = reminderParts[1].toInt()
        val reminderYear = reminderParts[2].toInt()

        return when {
            // Напоминание позднее года события
            reminderYear > eventYear -> false
            // Напоминание позднее месяца события
            reminderYear == eventYear && reminderMonth > eventMonth -> false
            // Напоминание позднее дня события
            reminderYear == eventYear && reminderMonth == eventMonth && reminderDay > eventDay -> false
            else -> true
        }
    }

    fun scheduleEventReminder(context: Context, eventID: Long, eventTitle: String, eventDate: String, reminderTimeStr: String) {
        // Формат даты и времени для парсинга
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val reminderDate = sdf.parse(reminderTimeStr)
        val reminderCal = Calendar.getInstance()
        reminderCal.time = reminderDate

        val intent = Intent(context, EventNotificationReceiver::class.java).apply {
            putExtra("event_title", eventTitle)
            putExtra("event_date", eventDate)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, eventID.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Планирование напоминания в зависимости от версии Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // API 31+ нужны специальные права
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderCal.timeInMillis, pendingIntent)
            } else {
                // Просим пользователя включить точное определение времени напоминания
                askToEnableExactAlarms(context)
            }
        } else {
            // Повторяющееся уведомление каждые сутки
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                reminderCal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }

    // Запрашивает разрешение на точное время напоминания
    private fun askToEnableExactAlarms(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            // Если версия ниже, сообщаем, что невозможно установить точное время напоминания
            Toast.makeText(context, "Установка точного будильника невозможна", Toast.LENGTH_SHORT).show()
        }
    }
}