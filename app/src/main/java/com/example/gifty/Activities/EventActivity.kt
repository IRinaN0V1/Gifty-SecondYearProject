package com.example.gifty.Activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.gifty.Api
import com.example.gifty.R
import com.example.gifty.ViewModels.EventViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EventActivity : AppCompatActivity() {
    // Внедрение зависимостей для API
    @Inject
    lateinit var api: Api
    private lateinit var eventName: TextView
    private lateinit var eventDate: TextView
    private lateinit var eventDescription: TextView
    private lateinit var reminderTime: TextView
    private val viewModel: EventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_event)

        // Получаем переданные данные из Intent
        val id = intent.getIntExtra("event_id", -1)

        // Инициализация элементов интерфейса
        eventName = findViewById(R.id.eventName)
        eventDate = findViewById(R.id.eventDate)
        eventDescription = findViewById(R.id.eventDescription)
        reminderTime = findViewById(R.id.reminderTime)
        val editBtn: ConstraintLayout = findViewById(R.id.editEventButton)
        val backBtn: ImageView = findViewById(R.id.backButton)
        val deleteBtn: ImageView = findViewById(R.id.deleteEventButton)

        // Наблюдаем за результатами загрузки события
        viewModel.eventResult.observe(this) { event ->
            if (event != null) {
                // Заполняем данные события на экране
                eventName.text = event.name
                eventDate.text = event.event_date
                // Если для события не указано описание
                eventDescription.text = if (event.description.trim().isEmpty()) {
                    "Не добавлено" // Отображаем сообщение "Не добавлено"
                } else {
                    event.description // Иначе показываем описание
                }
                // Установка времени напоминания
                reminderTime.text = event.reminder_time
            } else {
                // Сообщение об ошибке
                Toast.makeText(this, "Событие не найдено", Toast.LENGTH_SHORT).show()
                finish() // Закрываем активность
            }
        }

        // Наблюдаем за результатами удаления события
        viewModel.deleteResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Событие удалено", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Возникла ошибка. Пожалуйста, попробуйте еще раз.", Toast.LENGTH_SHORT).show()
            }
        }

        // Обработка нажатия на кнопку "Назад"
        backBtn.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        // Обработка нажатия на кнопку редактирования события
        editBtn.setOnClickListener {
            val intent = Intent(this@EventActivity, NewEventActivity::class.java)
            // Передаем данные события в новую активность для редактирования
            intent.putExtra("event_id", id)
            intent.putExtra("event_name", eventName.text.toString())
            intent.putExtra("event_date", eventDate.text.toString())
            intent.putExtra("event_description", eventDescription.text.toString())
            intent.putExtra("event_reminder_time", reminderTime.text.toString())
            startActivity(intent)
            this.overridePendingTransition(0, 0)
        }

        // Обработка нажатия на кнопку удаления события
        deleteBtn.setOnClickListener {
            // Создание диалога для подтверждения удаления
            AlertDialog.Builder(this@EventActivity).apply {
                setTitle("Подтверждение удаления")
                setMessage("Вы уверены, что хотите удалить это событие?")

                setPositiveButton("Да") { _, _ ->
                    // Удаляем событие через модель представления
                    viewModel.deleteEvent(id)
                }

                setNegativeButton("Нет") { dialog, _ ->
                    // Закрываем диалог при выборе "Нет"
                    dialog.dismiss()
                }
                // Показываем диалог
                create().show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Загружаем событие при переходе на активность
        val id = intent.getIntExtra("event_id", -1)
        viewModel.loadEvent(id)
    }
}