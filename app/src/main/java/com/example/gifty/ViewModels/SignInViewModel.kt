package com.example.gifty.ViewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifty.Interactors.UserInteractor
import com.example.gifty.Data.User
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userInteractor: UserInteractor,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _signInResult = MutableLiveData<User?>()
    val signInResult: LiveData<User?> = _signInResult

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            val user = userInteractor.getUser(email, password)
            _signInResult.postValue(user)
            if (user != null) {
                saveUserDetails(user.id, email)
            }
        }
    }

    private fun saveUserDetails(userId: Int, email: String) {
        with(sharedPreferences.edit()) {
            putInt("user_id", userId)
            putString("user_email", email)
            apply()
        }
    }
}