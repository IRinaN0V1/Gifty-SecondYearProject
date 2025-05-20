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
class EventViewModel  @Inject constructor(val eventInteractor: EventInteractor) : ViewModel() {
    private val _eventResult = MutableLiveData<Event?>()
    val eventResult: LiveData<Event?> = _eventResult

    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> = _deleteResult

    fun deleteEvent(eventId: Int) {
        viewModelScope.launch {
            val response = eventInteractor.deleteEvent(eventId)
            _deleteResult.postValue(response)
        }
    }

    fun loadEvent(eventId: Int) {
        viewModelScope.launch {
            val event = eventInteractor.getEventById(eventId)
            _eventResult.postValue(event)
        }
    }
}