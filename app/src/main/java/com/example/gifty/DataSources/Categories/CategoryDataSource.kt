package com.example.gifty.DataSources.Categories

import com.google.gson.JsonArray

interface CategoryDataSource {
    suspend fun getHolidays(): JsonArray
    suspend fun getHobbies(): JsonArray
    suspend fun getProfessions(): JsonArray
    suspend fun getAgeCategoryByAge(age: Int): Int
}