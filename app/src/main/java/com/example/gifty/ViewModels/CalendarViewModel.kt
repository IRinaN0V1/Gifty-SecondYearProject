package com.example.gifty.ViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifty.Data.Event
import com.example.gifty.Interactors.EventInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val eventInteractor: EventInteractor
) : ViewModel() {

    // переменная для хранения списка событий
    private val _events = MutableLiveData<List<Event>>()

    // переменная доступная для чтения
    val events: LiveData<List<Event>>
        get() = _events

    // Метод для получения событий по userId
    fun getEvents(userId: Int) {
        // Запускаем корутину
        viewModelScope.launch {
            // Получаем события с помощью интерактора
            val eventsList = eventInteractor.getEventsByUserId(userId)
            // Обновляем переменную
            _events.value = eventsList
        }
    }
}