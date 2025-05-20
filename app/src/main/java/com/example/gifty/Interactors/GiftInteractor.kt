package com.example.gifty.Interactors

import com.example.gifty.Data.Gift
import com.example.gifty.Repositories.GiftRepository
import com.google.gson.JsonArray

class GiftInteractor(private val giftRepository: GiftRepository) {
    suspend fun addGiftToForm(giftId: Int, formId: Int): Boolean{
        return giftRepository.addGiftToForm(giftId, formId)
    }
    suspend fun getGifts(): List<Gift> {
        return jsonConverter(giftRepository.getGifts())
    }
    suspend fun getSelectedGifts(formId: Int):  List<Gift> {
        return  jsonConverter(giftRepository.getSelectedGifts(formId))
    }
    suspend fun deleteSelectedGifts(formId: Int, giftIds: String): Boolean{
        return giftRepository.deleteSelectedGifts(formId, giftIds)
    }
    suspend fun getFoundGifts(genderId: String, ageCategoryId: Int, hobbies: String, professions: String, holidays: String): String?{
        return giftRepository.getFoundGifts(genderId, ageCategoryId, hobbies, professions, holidays)
    }
    suspend fun getGiftById(giftId: Int): Gift?{
        return giftRepository.getGiftById(giftId)
    }
    suspend fun getSelectedGiftByGiftIdAndFormId(giftId: Int, formId: Int): Boolean{
        return giftRepository.getSelectedGiftByGiftIdAndFormId(giftId, formId)
    }

    private fun jsonConverter(jsonArray: JsonArray): List<Gift> {
        val list = mutableListOf<Gift>()
        jsonArray.forEach { jsonElement ->
            val jsonObject = jsonElement.asJsonObject
            val listElement = Gift(
                id = jsonObject.get("id").asInt,
                name = jsonObject.get("name").asString,
                image = jsonObject.get("image").asString,
                description = jsonObject.get("description").asString,
                gender = jsonObject.get("gender").asInt
            )
            list.add(listElement)
        }
        return list
    }
}