package com.example.gifty.Interactors

import android.util.Log
import com.example.gifty.Data.Form
import com.example.gifty.Repositories.FormRepository
import com.google.gson.JsonArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FormInteractor(private val formsRepository: FormRepository) {

    suspend fun getFormsByUserId(userId: Int): List<Form> {
        return jsonConverter(formsRepository.getFormsByUserId(userId))
    }

    suspend fun deleteForm(formId: Int): Boolean {
        return formsRepository.deleteForm(formId)
    }

    suspend fun createForm(name: String, image: String?, birthday: String, userId: Int): Boolean {
        val convertedBirthday = convertBirthday(birthday)
        return formsRepository.createForm(name, image, convertedBirthday, userId)
    }

    suspend fun updateForm(formId: Int, newName: String, newBirthday: String, image: String): Boolean{
        val convertedBirthday = convertBirthday(newBirthday)
        return formsRepository.updateForm(formId, newName, convertedBirthday, image)
    }

    suspend fun getFormByUserIdAndName(userId: Int, name: String): Boolean {
        return formsRepository.getFormByUserIdAndName(userId, name)
    }

    suspend fun getFormById(formId: Int): Form? {
        return formsRepository.getFormById(formId)
    }

    private fun convertBirthday(birthday: String): String {
        return try {
            val formatFromInput = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val formatForDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = formatFromInput.parse(birthday)
            formatForDb.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    private fun jsonConverter(jsonArray: JsonArray): List<Form> {
        val list = mutableListOf<Form>()
        jsonArray.forEach { jsonElement ->
            val jsonObject = jsonElement.asJsonObject
            val formBirthday = jsonObject.get("birthday").asString

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.parse(formBirthday)

            val calendar = Calendar.getInstance()
            calendar.time = date

            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)

            val parsedBirthday = String.format("%02d.%02d.%d", day, month, year)

            val listElement = Form(
                id = jsonObject.get("id").asInt,
                name = jsonObject.get("name").asString,
                birthday = parsedBirthday,
                image = jsonObject.get("image").asString
            )
            list.add(listElement)
        }
        return list
    }
}