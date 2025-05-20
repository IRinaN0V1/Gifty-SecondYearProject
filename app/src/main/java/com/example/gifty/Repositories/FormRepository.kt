package com.example.gifty.Repositories

import com.example.gifty.DataSources.Forms.FormDataSourceImpl
import com.example.gifty.Data.Form
import com.example.gifty.DataSources.Forms.FormDataSource
import com.google.gson.JsonArray

class FormRepository(private val formDataSource: FormDataSource) {
    suspend fun getFormsByUserId(userId: Int): JsonArray {
        return formDataSource.getFormsByUserId(userId)
    }
    suspend fun getFormByUserIdAndName(userId: Int, name: String): Boolean {
        return formDataSource.getFormByUserIdAndName(userId, name)
    }
    suspend fun getFormById(formId: Int): Form? {
        return formDataSource.getFormById(formId)
    }
    suspend fun createForm(name: String, image: String?, birthday: String, userId: Int): Boolean {
        return formDataSource.createForm(name, image, birthday, userId)
    }
    suspend fun deleteForm(formId: Int): Boolean{
        return formDataSource.deleteForm(formId)
    }
    suspend fun updateForm(formId: Int, newName: String, newBirthday: String, image: String): Boolean{
        return formDataSource.updateForm(formId, newName, newBirthday, image)
    }
}