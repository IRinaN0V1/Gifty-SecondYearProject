package com.example.gifty.DataSources.Forms

import com.example.gifty.Data.Form
import com.google.gson.JsonArray

interface FormDataSource {
    suspend fun getFormsByUserId(userId: Int): JsonArray
    suspend fun getFormById(formId: Int): Form?
    suspend fun createForm(name: String, image: String?, birthday: String, userId: Int): Boolean
    suspend fun getFormByUserIdAndName(userId: Int, name: String): Boolean
    suspend fun deleteForm(formId: Int): Boolean
    suspend fun updateForm(formId: Int, newName: String, newBirthday: String, image: String): Boolean
}