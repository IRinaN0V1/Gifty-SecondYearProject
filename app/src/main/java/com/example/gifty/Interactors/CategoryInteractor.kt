package com.example.gifty.Interactors

import com.example.gifty.Data.CategoryData
import com.example.gifty.Repositories.CategoryDataRepository
import com.google.gson.JsonArray


class CategoryInteractor(private val categoriesRepository: CategoryDataRepository) {

    suspend fun getHolidays(): List<CategoryData> {
        return jsonConverter(categoriesRepository.getHolidays())
    }
    suspend fun getHobbies(): List<CategoryData> {
        return jsonConverter(categoriesRepository.getHobbies())
    }
    suspend fun getProfessions(): List<CategoryData> {
        return jsonConverter(categoriesRepository.getProfessions())
    }
    suspend fun getAgeCategoryByAge(age: Int): Int{
        return categoriesRepository.getAgeCategoryByAge(age)
    }

    private fun jsonConverter(jsonArray: JsonArray): List<CategoryData> {
        val list = mutableListOf<CategoryData>()
        jsonArray.forEach { jsonElement ->
            val jsonObject = jsonElement.asJsonObject
            val listElement = CategoryData(
                id = jsonObject.get("id").asInt,
                name = jsonObject.get("name").asString
            )
            list.add(listElement)
        }
        return list
    }
}