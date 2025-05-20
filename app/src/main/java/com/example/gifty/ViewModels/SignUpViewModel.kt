package com.example.gifty.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifty.Interactors.UserInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel@Inject constructor(
    val userInteractor: UserInteractor
) : ViewModel() {
    private val _signUpResult = MutableLiveData<Boolean>()
    val signUpResult: LiveData<Boolean> = _signUpResult

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            val success = userInteractor.createUser(email, password)
            _signUpResult.postValue(success)
        }
    }

    fun validateSignUp(email: String, password: String, confirmPassword: String): Boolean {
        return userInteractor.validateEmail(email) &&
                userInteractor.validatePassword(password) &&
                userInteractor.passwordsMatch(password, confirmPassword)
    }
}
