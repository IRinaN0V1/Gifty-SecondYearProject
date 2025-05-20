package com.example.gifty.ViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifty.Data.Gift
import com.example.gifty.Interactors.GiftInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoundGiftsViewModel @Inject constructor(
    private val giftInteractor: GiftInteractor
) : ViewModel() {

    private val _giftsList = MutableLiveData<List<Gift>>()
    val giftsList: LiveData<List<Gift>> get() = _giftsList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun fetchFoundGifts(selectedGenderId: String, age: Int, hobbies: String, professions: String, holidays: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val foundGifts = giftInteractor.getFoundGifts(selectedGenderId, age, hobbies, professions, holidays)
                if (!foundGifts.isNullOrEmpty()) {
                    val giftIds = foundGifts.split(",").map { it.trim() }
                    val gifts = mutableListOf<Gift>()

                    for (id in giftIds) {
                        giftInteractor.getGiftById(id.toInt())?.let { gifts.add(it) }
                    }
                    _giftsList.value = gifts
                } else {
                    _giftsList.value = emptyList()
                    _errorMessage.value = "Нет доступных подарков"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}