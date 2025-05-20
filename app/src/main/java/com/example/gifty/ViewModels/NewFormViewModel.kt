package com.example.gifty.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifty.Interactors.FormInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewFormViewModel @Inject constructor(
    private val formInteractor: FormInteractor
) : ViewModel() {
    private val _createResult = MutableLiveData<Boolean>()
    val createResult: LiveData<Boolean> = _createResult

    private val _changeFormResult = MutableLiveData<Boolean>()
    val changeFormResult: LiveData<Boolean> = _changeFormResult

    private val _checkIfFormExistsResult = MutableLiveData<Boolean>()
    val checkIfFormExistsResult: LiveData<Boolean> = _checkIfFormExistsResult

    fun createForm(name: String, image: String?, birthday: String, userId: Int) {
        viewModelScope.launch {
            val response = formInteractor.createForm(name, image, birthday, userId)
            _createResult.postValue(response)
        }
    }

    fun updateForm(formId: Int, newName: String, newBirthday: String, image: String) {
        viewModelScope.launch {
            val response = formInteractor.updateForm(formId, newName, newBirthday, image)
            _changeFormResult.postValue(response)
        }
    }

    fun checkIfFormExists(userId: Int, name: String) {
        viewModelScope.launch {
            val response = formInteractor.getFormByUserIdAndName(userId, name)
            _checkIfFormExistsResult.postValue(response)
        }
    }
}