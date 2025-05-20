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
class ChangePasswordViewModel @Inject constructor(val userInteractor: UserInteractor, @ApplicationContext private val appContext: Context) : ViewModel() {
    private val _changePasswordResult = MutableLiveData<Boolean>()
    val changePasswordResult: LiveData<Boolean> = _changePasswordResult
    fun changePassword(userId: Int, newPassword: String) {
        viewModelScope.launch {
            val response = userInteractor.updateUser(userId, newPassword)
            _changePasswordResult.postValue(response)
        }
    }
}