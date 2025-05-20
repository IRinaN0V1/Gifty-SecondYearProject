package com.example.gifty.ViewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifty.Data.CategoryData
import com.example.gifty.Interactors.CategoryInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryInteractor: CategoryInteractor,
    private val application: Application
) : ViewModel() {

    // переменные для хранения списка категорий
    private val _categories = MutableLiveData<List<CategoryData>>()
    val categories: LiveData<List<CategoryData>> get() = _categories

    // Переменные для обработки ошибок
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Переменные для получения состояния загрузки
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    val selectedIds = mutableListOf<Int>()
    val selectedNames = mutableListOf<String>()
    var currentCategory: String = ""

    // Метод для загрузки категорий
    fun loadCategories(categoryName: String) {
        currentCategory = categoryName
        // Запускаем корутину
        viewModelScope.launch {
            val categories = when (categoryName) {
                "hobbies" -> categoryInteractor.getHobbies()
                "holidays" -> categoryInteractor.getHolidays()
                "professions" -> categoryInteractor.getProfessions()
                else -> throw IllegalArgumentException("Неверная категория: $categoryName")
            }
            _categories.postValue(categories) // Обновляем данные
        }
    }

    // Метод для сохранения выбранных значений в SharedPreferences
    fun saveSelectedValues() {
        val sharedPref = application.getSharedPreferences("categories_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Сохраняем идентификаторы и имена выбранных категорий
        editor.putStringSet("selected_${currentCategory}_ids", HashSet(selectedIds.map { it.toString() }))
        editor.putStringSet("selected_${currentCategory}_names", HashSet(selectedNames))
        editor.apply()
    }

    // Метод для восстановления ранее выбранных значений из SharedPreferences
    fun restoreSelectedValues(type: String) {
        val sharedPref = application.getSharedPreferences("categories_prefs", Context.MODE_PRIVATE)
        val savedIds = sharedPref.getStringSet("selected_${type}_ids", emptySet())
        savedIds?.forEach { value ->
            val id = value.trim().toIntOrNull()
            if (id != null && !selectedIds.contains(id)) {
                selectedIds.add(id)
            }
        }
        val savedNames = sharedPref.getStringSet("selected_${type}_names", emptySet())
        savedNames?.filterNotNull()?.forEach { name ->
            if (!selectedNames.contains(name)) {
                selectedNames.add(name)
            }
        }
    }
}