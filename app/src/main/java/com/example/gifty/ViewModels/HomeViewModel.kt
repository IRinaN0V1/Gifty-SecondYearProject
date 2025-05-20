package com.example.gifty.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifty.Data.Form
import com.example.gifty.Interactors.FormInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel  @Inject constructor(val formsInteractor: FormInteractor) : ViewModel() {
    private val _forms = MutableLiveData<List<Form>>()
    val forms: LiveData<List<Form>> get() = _forms

    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> = _deleteResult


    fun getForms(userId: Int) {
        viewModelScope.launch {
            val formsList = formsInteractor.getFormsByUserId(userId)
            _forms.value = formsList
        }
    }

    fun deleteForm(formId: Int) {
        viewModelScope.launch {
            val response = formsInteractor.deleteForm(formId)
            _deleteResult.postValue(response)
        }
    }
}