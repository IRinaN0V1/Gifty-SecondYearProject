package com.example.gifty.ViewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifty.Data.Gift
import com.example.gifty.Interactors.GiftInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormViewModel @Inject constructor(
    private val giftInteractor: GiftInteractor,
    private val application: Application
) : ViewModel() {

    private val _gifts = MutableLiveData<List<Gift>>()
    val gifts: LiveData<List<Gift>> get() = _gifts

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    fun loadSelectedGifts(formId: Int) {
        viewModelScope.launch {
            try {
                val giftsResponse = giftInteractor.getSelectedGifts(formId)
                _gifts.postValue(giftsResponse)
            } catch (e: Exception) {
                Log.e("MyLog", "Ошибка загрузки подарков: ${e.message}")
                _message.postValue("Ошибка загрузки подарков.")
            }
        }
    }

    fun deleteSelectedGifts(formId: Int, selectedGiftIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val giftIds = selectedGiftIds.joinToString(",")
                val response = giftInteractor.deleteSelectedGifts(formId, giftIds)
                if (response) {
                    // Обновляем список подарков после удаления
                    val currentGifts = _gifts.value ?: listOf()
                    val updatedGifts = currentGifts.filter { gift ->
                        !selectedGiftIds.contains(gift.id.toString())
                    }
                    _gifts.postValue(updatedGifts)
                } else {
                    _message.postValue("Не удалось удалить подарки")
                }
            } catch (e: Exception) {
                Log.e("MyLog", "Ошибка удаления подарков: ${e.message}")
                _message.postValue("Ошибка удаления подарков.")
            }
        }
    }
}