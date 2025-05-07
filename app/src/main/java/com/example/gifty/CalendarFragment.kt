package com.example.gifty

import android.content.Context
import java.util.Calendar
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.google.gson.JsonArray
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class CalendarFragment : Fragment() {
    @Inject
    lateinit var api: Api
    private lateinit var addNewEvent: ConstraintLayout
    private lateinit var calendarView: com.applandeo.materialcalendarview.CalendarView
    private var selectedDate: String? = null
    private var eventMap: Map<String, Event>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Инициализация кнопки добавления нового события
        addNewEvent = view.findViewById(R.id.addNewEvent)
        calendarView = view.findViewById(R.id.calendarView)
        val currentDay = Calendar.getInstance()

        val currentDayYear = currentDay.get(Calendar.YEAR)
        val currentDayMonth = currentDay.get(Calendar.MONTH)
        val currentDayDay = currentDay.get(Calendar.DAY_OF_MONTH)
        val currentDate = String.format("%d-%02d-%02d", currentDayYear, currentDayMonth + 1, currentDayDay)

        selectedDate = currentDate


        addNewEvent.setOnClickListener {
            // Проверяем, была ли выбрана дата
            if (selectedDate.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Выберите дату", Toast.LENGTH_SHORT).show()
            } else if (eventMap?.containsKey(selectedDate!!) == true) {
                // Если событие для выбранной даты уже существует
                Toast.makeText(requireContext(), "Событие уже создано для этой даты", Toast.LENGTH_SHORT).show()
            }else {
                // Передаем выбранную дату в интенте
                val intent = Intent(requireContext(), NewEventActivity::class.java)
                val date = parseDate(selectedDate)
                intent.putExtra("SELECTED_DATE", date)
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
            }
        }
        val settingsBtn: ImageView = view.findViewById(R.id.settings)
        settingsBtn.setOnClickListener {
            // Создаем Intent, используя context текущей активности
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            requireActivity().startActivity(intent)
            // Убираем анимацию перехода
            requireActivity().overridePendingTransition(0, 0)
        }


        calendarView.setOnCalendarDayClickListener(object : OnCalendarDayClickListener {
            override fun onClick(calendarDay: CalendarDay) {
                val clickedDayCalendar = calendarDay.calendar
                val selectedYear = clickedDayCalendar.get(Calendar.YEAR)
                val selectedMonth = clickedDayCalendar.get(Calendar.MONTH)
                val selectedDay = clickedDayCalendar.get(Calendar.DAY_OF_MONTH)
                val clickedDay = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)

                // Проверяем, есть ли событие для этой даты
                eventMap?.get(clickedDay)?.let { event ->
                    // Если событие найдено, открываем информацию о событии
                    val intent = Intent(requireContext(), EventActivity::class.java)
                    intent.putExtra("event_id", event.id)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(0, 0)
                } ?: run {
                    // Если события нет, обновляем selectedDate
                    selectedDate = clickedDay
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadEvents()
    }

    private fun parseDate(event: String?): String? {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date: Date = dateFormat.parse(event) ?: return null

            val calendar = Calendar.getInstance()
            calendar.time = date

            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1 // Месяцы начинаются с 0
            val year = calendar.get(Calendar.YEAR)

            String.format("%02d.%02d.%d", day, month, year)
        } catch (e: Exception) {
            // Логирование ошибки
            Log.e("MyLog", "Ошибка разбора даты: ${event}", e)
            null // В случае ошибки вернуть null
        }
    }

    private fun jsonConverter(jsonArray: JsonArray): List<Event> {
        val list = mutableListOf<Event>()
        jsonArray.forEach { jsonElement ->
            val jsonObject = jsonElement.asJsonObject
            val listElement = Event(
                id = jsonObject.get("id").asInt,
                name = jsonObject.get("name").asString,
                reminder_time = jsonObject.get("reminder_time").asString,
                description = jsonObject.get("description").asString,
                event_date = jsonObject.get("event_date").asString
            )
            list.add(listElement)
        }
        return list
    }

    private fun loadEvents() {
        val sp = requireContext().getSharedPreferences("PC", Context.MODE_PRIVATE)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getEventsByUserId(sp.getInt("UserId", -1))
                val jsonResponse = response.body()
                if (jsonResponse != null && !jsonResponse.get("error").asBoolean) {
                    val eventsArray = jsonResponse.getAsJsonArray("events")
                    val eventsList = jsonConverter(eventsArray)
                    eventMap = eventsList.associateBy { it.event_date }
                    Log.d("MyLog", "${eventMap}")

                    // Создаем изменяемый список для хранения выделенных дней
                    val calendars = mutableListOf<Calendar>()

                    // Получаем список дней событий и добавляем их в calendars
                    eventsList.forEach { event ->
                        val parts = event.event_date.split("-")
                        val calendarDay = Calendar.getInstance().apply {
                            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                        }
                        calendars.add(calendarDay)
                    }
                    // Включаем выделенные дни в календарь
                    withContext(Dispatchers.Main) {
                        calendarView.setDisabledDays(calendars);
                    }
                } else {
                    Toast.makeText(requireContext(), jsonResponse!!.get("message").asString, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("MyLog", e.message ?: "Unknown exception")
            }
        }
    }
}