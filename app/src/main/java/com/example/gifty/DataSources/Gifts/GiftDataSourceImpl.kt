package com.example.gifty.DataSources.Gifts

import android.util.Log
import com.example.gifty.Api
import com.example.gifty.Data.Gift
import com.google.gson.JsonArray

class GiftDataSourceImpl(private val api: Api) : GiftDataSource {
    override suspend fun addGiftToForm(giftId: Int, formId: Int): Boolean{
        return try {
            val response = api.addGiftToForm(giftId, formId)
            val jsonResponse = response.body()
            jsonResponse?.get("error")?.asBoolean == false
        } catch (e: Exception) {
            false
        }
    }
    override suspend fun getGifts(): JsonArray{
        return try {
            val response = api.getGifts()
            val jsonResponse = response.body()
            if (jsonResponse != null && !jsonResponse.get("error").asBoolean) {
                val giftsArray = jsonResponse.getAsJsonArray("gifts")
                return giftsArray
            } else {
                JsonArray()
            }
        } catch (e: Exception) {
            JsonArray()
        }
    }
    override suspend fun getSelectedGifts(formId: Int):  JsonArray{
        return try {
            val response = api.getSelectedGifts(formId)
            val jsonResponse = response.body()
            if (jsonResponse != null && !jsonResponse.get("error").asBoolean) {
                val giftsArray = jsonResponse.getAsJsonArray("selectedgifts")
                return giftsArray
            } else {
                JsonArray()
            }
        } catch (e: Exception) {
            JsonArray()
        }
    }
    override suspend fun deleteSelectedGifts(formId: Int, giftIds: String): Boolean{
        return try {
            val response = api.deleteSelectedGifts(formId, giftIds)
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
    override suspend fun getFoundGifts(genderId: String, ageCategoryId: Int, hobbies: String, professions: String, holidays: String): String?{
        return try {
            val response = api.getFoundGifts(genderId, ageCategoryId, hobbies, professions, holidays)
            val jsonResponse = response.body()
            if (jsonResponse != null) {
                if (!jsonResponse.get("error").asBoolean){
                    val foundGifts = jsonResponse.get("foundgifts").asString
                    foundGifts
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

    override suspend fun getGiftById(giftId: Int): Gift?{
        return try {
            val response = api.getGiftById(giftId)
            val jsonResponse = response.body()
            if (jsonResponse != null) {
                if (!jsonResponse.get("error").asBoolean){
                    jsonResponse.let {
                        val giftJson = it.getAsJsonObject("gift")
                        Gift(giftJson.get("id").asInt, giftJson.get("name").asString, giftJson.get("image").asString, giftJson.get("description").asString, giftJson.get("gender").asInt)
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
    override suspend fun getSelectedGiftByGiftIdAndFormId(giftId: Int, formId: Int): Boolean{
        return try {
            val response = api.getSelectedGiftByGiftIdAndFormId(giftId, formId)
            val jsonResponse = response.body()
            jsonResponse?.get("flag")?.asBoolean ?: false
        } catch (e: Exception) {
            false
        }
    }
}