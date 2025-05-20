package com.example.gifty.DataSources.Forms

import com.example.gifty.Api
import com.example.gifty.Data.Form
import com.google.gson.JsonArray

class FormDataSourceImpl(private val api: Api) : FormDataSource {
    override suspend fun getFormsByUserId(userId: Int): JsonArray {
        return try {
            val response = api.getFormsByUserId(userId)
            val jsonResponse = response.body()
            if (jsonResponse != null && !jsonResponse.get("error").asBoolean) {
                val formsArray = jsonResponse.getAsJsonArray("forms")
                return formsArray
            } else {
                JsonArray()
            }
        } catch (e: Exception) {
            JsonArray()
        }
    }

    override suspend fun getFormById(formId: Int): Form? {
        return try {
            val response = api.getFormById(formId)
            val jsonResponse = response.body()
            if (jsonResponse != null) {
                if (!jsonResponse.get("error").asBoolean){
                    jsonResponse.let {
                        val formJson = it.getAsJsonObject("form")
                        Form(formJson.get("id").asInt, formJson.get("name").asString, formJson.get("birthday").asString,  formJson.get("image").asString)
                    }
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getFormByUserIdAndName(userId: Int, name: String): Boolean {
        return try {
            val response = api.getFormByUserIdAndName(name, userId)
            val jsonResponse = response.body()
            jsonResponse?.get("flag")?.asBoolean ?: false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun createForm(name: String, image: String?, birthday: String, userId: Int): Boolean {
        return try {
            val response = api.createForm(name, image, birthday, userId)
            val jsonResponse = response.body()
            jsonResponse?.get("error")?.asBoolean == false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteForm(formId: Int): Boolean {
        return try {
            val response = api.deleteForm(formId)
            val jsonResponse = response.body()
            if (jsonResponse != null) {
                jsonResponse.get("error")?.asBoolean == false
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateForm(formId: Int, newName: String, newBirthday: String, image: String): Boolean {
        return try {
            val response = api.updateForm(formId, newName, newBirthday, image)
            val jsonResponse = response.body()
            if (jsonResponse != null) {
                jsonResponse.get("error")?.asBoolean == false
            }else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}