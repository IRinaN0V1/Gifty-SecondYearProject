package com.example.gifty.ViewModels

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
class SearchViewModel @Inject constructor(
    private val giftsInteractor: GiftInteractor
) : ViewModel() {

    private val _allGifts = MutableLiveData<List<Gift>>()
    val allGifts: LiveData<List<Gift>> = _allGifts

    private val _filteredGifts = MutableLiveData<List<Gift>>()
    val filteredGifts: LiveData<List<Gift>> = _filteredGifts

    fun getGifts() {
        viewModelScope.launch {
            val giftsList = giftsInteractor.getGifts()
            _allGifts.value = giftsList
        }
    }

    fun filterGifts(query: String) {
        val currentGifts = _allGifts.value ?: emptyList()
        val filteredResult = currentGifts.filter { it.name.contains(query, ignoreCase = true) }
        _filteredGifts.value = filteredResult
    }

    fun clearFilter() {
        _filteredGifts.value = _allGifts.value
    }
}