package com.example.gifty.ViewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifty.Interactors.CategoryInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParametersSearchViewModel @Inject constructor(
    private val categoryInteractor: CategoryInteractor,
    private val application: Application
) : ViewModel() {
    private val _ageId = MutableLiveData<Int>()
    val ageId: LiveData<Int> get() = _ageId


    fun getSelectedIds(key: String): List<Int> {
        val sharedPref = application.getSharedPreferences("categories_prefs", Application.MODE_PRIVATE)
        return sharedPref.getStringSet(key, emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?: emptyList()
    }
}