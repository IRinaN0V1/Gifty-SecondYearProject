package com.example.gifty.Repositories

import com.example.gifty.DataSources.Gifts.GiftDataSourceImpl
import com.example.gifty.Data.Gift
import com.example.gifty.DataSources.Gifts.GiftDataSource
import com.google.gson.JsonArray

class GiftRepository(private val giftDataSource: GiftDataSource) {
    suspend fun addGiftToForm(giftId: Int, formId: Int): Boolean{
        return giftDataSource.addGiftToForm(giftId, formId)
    }
    suspend fun getGifts(): JsonArray{
        return giftDataSource.getGifts()
    }
    suspend fun getSelectedGifts(formId: Int):  JsonArray{
        return giftDataSource.getSelectedGifts(formId)
    }
    suspend fun deleteSelectedGifts(formId: Int, giftIds: String): Boolean{
        return giftDataSource.deleteSelectedGifts(formId, giftIds)
    }
    suspend fun getFoundGifts(genderId: String, ageCategoryId: Int, hobbies: String, professions: String, holidays: String): String?{
        return giftDataSource.getFoundGifts(genderId, ageCategoryId, hobbies, professions, holidays)
    }
    suspend fun getGiftById(giftId: Int): Gift?{
        return giftDataSource.getGiftById(giftId)
    }
    suspend fun getSelectedGiftByGiftIdAndFormId(giftId: Int, formId: Int): Boolean{
        return giftDataSource.getSelectedGiftByGiftIdAndFormId(giftId, formId)
    }
}