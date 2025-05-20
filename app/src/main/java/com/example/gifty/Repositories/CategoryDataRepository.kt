package com.example.gifty.Repositories

import com.example.gifty.DataSources.Categories.CategoryDataSource
import com.example.gifty.DataSources.Categories.CategoryDataSourceImpl
import com.google.gson.JsonArray

class CategoryDataRepository(private val categoryDataSource: CategoryDataSource) {
    suspend fun getHolidays(): JsonArray{
        return categoryDataSource.getHolidays()
    }
    suspend fun getHobbies(): JsonArray{
        return categoryDataSource.getHobbies()
    }
    suspend fun getProfessions(): JsonArray{
        return categoryDataSource.getProfessions()
    }
    suspend fun getAgeCategoryByAge(age: Int): Int{
        return categoryDataSource.getAgeCategoryByAge(age)
    }
}