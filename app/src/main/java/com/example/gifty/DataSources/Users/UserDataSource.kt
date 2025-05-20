package com.example.gifty.DataSources.Users

import com.example.gifty.Data.User

interface UserDataSource {
    suspend fun getUser(email: String, password: String): User?
    suspend fun createUser(email: String, password: String): Boolean
    suspend fun getUserByEmail(email: String): Boolean
    suspend fun deleteUser(userId: Int): Boolean
    suspend fun updateUser(userId: Int, newPassword: String): Boolean
}


