package com.example.gifty.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifty.Interactors.EventInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewEventViewModel @Inject constructor(
    private val eventInteractor: EventInteractor
) : ViewModel() {

    private val _createResult = MutableLiveData<Boolean>()
    val createResult: LiveData<Boolean> = _createResult

    private val _changeEventResult = MutableLiveData<Boolean>()
    val changeEventResult: LiveData<Boolean> = _changeEventResult


    fun createEvent(userId: Int, name: String, reminderTime: String, description: String, eventDate: String) {
        viewModelScope.launch {
            val response = eventInteractor.createEvent(userId, name, reminderTime, description, eventDate)
            _createResult.postValue(response)
        }
    }

    fun updateEvent(eventId: Int, name: String, reminder_time: String, description: String) {
        viewModelScope.launch {
            val response = eventInteractor.updateEvent(eventId, name, reminder_time, description)
            _changeEventResult.postValue(response)
        }
    }
}