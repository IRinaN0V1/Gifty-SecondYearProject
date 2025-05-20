package com.example.gifty.DataSources.Categories

import com.example.gifty.Api
import com.google.gson.JsonArray

class CategoryDataSourceImpl(private val api: Api) : CategoryDataSource {

    // Метод возвращает список праздников из API
    override suspend fun getHolidays(): JsonArray {
        return try {
            val response = api.getHolidays()                  // Отправляем запрос к API для получения списка праздников
            val jsonResponse = response.body()                // Извлекаем тело ответа
            if (jsonResponse != null && !jsonResponse.get("error").asBoolean) { // Проверяем успешность запроса
                val holidaysArray = jsonResponse.getAsJsonArray("holidays") // Получаем массив праздников
                holidaysArray                                  // Возвращаем массив праздников
            } else {
                JsonArray()                                   // В случае ошибки возвращаем пустой массив
            }
        } catch (e: Exception) {
            JsonArray()                                       // Ловим исключения и возвращаем пустой массив
        }
    }

    //Метод возвращает список увлечений из API
    override suspend fun getHobbies(): JsonArray {
        return try {
            val response = api.getHobbies()                   // Запрашиваем список хобби
            val jsonResponse = response.body()                // Извлекаем тело ответа
            if (jsonResponse != null && !jsonResponse.get("error").asBoolean) { // Проверяем отсутствие ошибки
                val hobbiesArray = jsonResponse.getAsJsonArray("hobbies") // Получаем массив хобби
                hobbiesArray                                 // Возвращаем массив хобби
            } else {
                JsonArray()                                   // Иначе возвращаем пустой массив
            }
        } catch (e: Exception) {
            JsonArray()                                       // Отлавливаем исключение и возвращаем пустой массив
        }
    }

    // Метод возвращает список профессий из API
    override suspend fun getProfessions(): JsonArray {
        return try {
            val response = api.getProfessions()               // Запрашиваем профессии у API
            val jsonResponse = response.body()                // Извлекаем тело ответа
            if (jsonResponse != null && !jsonResponse.get("error").asBoolean) { // Проверяем успех запроса
                val professionsArray = jsonResponse.getAsJsonArray("professions") // Получаем массив профессий
                professionsArray                             // Возвращаем массив профессий
            } else {
                JsonArray()                                   // Иначе возвращаем пустой массив
            }
        } catch (e: Exception) {
            JsonArray()                                       // Ловим исключение и возвращаем пустой массив
        }
    }

    // Метод возвращает идентификатор возрастной категории по возрасту
    override suspend fun getAgeCategoryByAge(age: Int): Int {
        return try {
            val response = api.getAgeCategoryByAge(age)       // Запрашиваем категорию возраста по указанному возрасту
            val jsonResponse = response.body()                // Извлекаем тело ответа
            if (jsonResponse != null) {
                if (!jsonResponse.get("error").asBoolean){    // Проверяем отсутствие ошибки
                    val ageId = jsonResponse.get("agecategoryid").asInt // Получаем ID категории возраста
                    ageId                                     // Возвращаем идентификатор категории
                } else {
                    -1                                        // В случае ошибки возвращаем -1
                }
            } else {
                -1                                            // Если тело ответа пустое, возвращаем -1
            }
        } catch (e: Exception) {
            -1                                                // Ловим исключение и возвращаем -1
        }
    }
}