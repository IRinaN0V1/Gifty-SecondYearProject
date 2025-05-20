package com.example.gifty.DataSources.Users

import com.example.gifty.Api
import com.example.gifty.Data.User

class UserDataSourceImpl(private val api: Api) : UserDataSource {
    override suspend fun createUser(email: String, password: String): Boolean {
        return try {
            val isUserExist = getUserByEmail(email)
            if (!isUserExist) {
                val response = api.createUser(email, password)
                val jsonResponse = response.body()
                jsonResponse?.get("error")?.asBoolean == false
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUser(email: String, password: String): User? {
        val response= api.getUser(email, password)
        val jsonResponse = response.body()
        if (jsonResponse != null) {
            if (!jsonResponse.get("error").asBoolean){
                jsonResponse.let {
                    val userJson = it.getAsJsonObject("user")
                    return User(userJson.get("id").asInt, userJson.get("email").asString, userJson.get("password").asString)
                }
            } else {
                return null
            }
        } else {
            return null
        }
    }

    override suspend fun getUserByEmail(email: String): Boolean {
        return try {
            val response = api.getUserByEmail(email)
            val jsonResponse = response.body()
            jsonResponse?.get("flag")?.asBoolean ?: false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteUser(userId: Int): Boolean {
        return try {
            val response = api.deleteUser(userId)
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

    override suspend fun updateUser(userId: Int, newPassword: String): Boolean {
        return try {
            val response = api.updateUserPassword(userId, newPassword)
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
}