package com.example.gifty.ViewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifty.Interactors.UserInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class MyAccountViewModel @Inject constructor(private val userInteractor: UserInteractor, @ApplicationContext private val appContext: Context) : ViewModel() {
    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> = _deleteResult

    fun deleteAccount(userId: Int) {
        viewModelScope.launch {
            val response = userInteractor.deleteUser(userId)
            _deleteResult.postValue(response)
        }
    }

    fun clearUserPreferences() {
        val prefs = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}