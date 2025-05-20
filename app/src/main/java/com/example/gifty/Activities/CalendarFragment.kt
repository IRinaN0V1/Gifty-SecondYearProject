package com.example.gifty.Activities

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
import androidx.fragment.app.viewModels
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.example.gifty.Api
import com.example.gifty.Data.Event
import com.example.gifty.R
import com.example.gifty.ViewModels.CalendarViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CalendarFragment : Fragment() {
    @Inject
    lateinit var api: Api

    private val viewModel: CalendarViewModel by viewModels()
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

        // Получаем текущую дату
        val currentDay = Calendar.getInstance()
        val currentDayYear = currentDay.get(Calendar.YEAR)
        val currentDayMonth = currentDay.get(Calendar.MONTH)
        val currentDayDay = currentDay.get(Calendar.DAY_OF_MONTH)
        val currentDate = String.format("%02d.%02d.%d", currentDayDay, currentDayMonth + 1, currentDayYear)

        // Устанавливаем текущую дату как выбранную
        selectedDate = currentDate

        // Обработчик нажатия на кнопку добавления события
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
                intent.putExtra("SELECTED_DATE", selectedDate)
                startActivity(intent)
                // Убираем анимацию перехода
                requireActivity().overridePendingTransition(0, 0)
            }
        }
        val settingsBtn: ImageView = view.findViewById(R.id.settings)
        // Обработчик нажатия на кнопку настроек
        settingsBtn.setOnClickListener {
            // Создаем Intent для перехода на экран настроек
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            requireActivity().startActivity(intent)
            // Убираем анимацию перехода
            requireActivity().overridePendingTransition(0, 0)
        }

        // Устанавливаем слушатель для нажатия на дни календаря
        calendarView.setOnCalendarDayClickListener(object : OnCalendarDayClickListener {
            override fun onClick(calendarDay: CalendarDay) {
                // Получаем выбранную дату
                val clickedDayCalendar = calendarDay.calendar
                val selectedYear = clickedDayCalendar.get(Calendar.YEAR)
                val selectedMonth = clickedDayCalendar.get(Calendar.MONTH)
                val selectedDay = clickedDayCalendar.get(Calendar.DAY_OF_MONTH)
                val clickedDay = String.format("%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear)

                // Проверяем, есть ли событие для этой даты
                eventMap?.get(clickedDay)?.let { event ->
                    // Если событие найдено, то отображаем информацию о нем
                    val intent = Intent(requireContext(), EventActivity::class.java)
                    intent.putExtra("event_id", event.id)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(0, 0)
                } ?: run {
                    // Если события нет, обновляем выбранную дату
                    selectedDate = clickedDay
                }
            }
        })

        // Наблюдаем за изменениями списка событий
        viewModel.events.observe(viewLifecycleOwner) { events ->
            // Отображение созданных событий
            displayEvents(events)
        }
    }

    override fun onResume() {
        super.onResume()
        // Загружаем события при переходе на фрагмент
        loadEvents()
    }

    private fun loadEvents() {
        // Получаем данные пользователя
        val sharedPrefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getInt("user_id", -1)
        // Получаем созданные пользователем события
        viewModel.getEvents(userId)
    }

    private fun displayEvents(eventsList: List<Event>) {
        eventMap = eventsList.associateBy { it.event_date }
        val calendars = mutableListOf<Calendar>()

        // Получаем список событий и добавляем их в массив
        eventsList.forEach { event ->
            val parts = event.event_date.split(".")
            val calendarDay = Calendar.getInstance().apply {
                set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
            }
            // Добавляем день события в список
            calendars.add(calendarDay)
        }
        // Устанавливаем дни, для которых уже созданы события
        calendarView.setDisabledDays(calendars);
    }
}
