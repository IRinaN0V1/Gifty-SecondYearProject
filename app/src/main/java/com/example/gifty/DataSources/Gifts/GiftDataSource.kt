package com.example.gifty.DataSources.Gifts

import com.example.gifty.Data.Gift
import com.google.gson.JsonArray

interface GiftDataSource {
    suspend fun addGiftToForm(giftId: Int, formId: Int): Boolean
    suspend fun getGifts(): JsonArray
    suspend fun getSelectedGifts(formId: Int):  JsonArray
    suspend fun deleteSelectedGifts(formId: Int, giftIds: String): Boolean
    suspend fun getFoundGifts(genderId: String, ageCategoryId: Int, hobbies: String, professions: String, holidays: String): String?
    suspend fun getGiftById(giftId: Int): Gift?
    suspend fun getSelectedGiftByGiftIdAndFormId(giftId: Int, formId: Int): Boolean
}