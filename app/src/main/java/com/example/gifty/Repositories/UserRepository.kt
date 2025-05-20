package com.example.gifty.Repositories

import com.example.gifty.DataSources.Users.UserDataSourceImpl
import com.example.gifty.Data.User
import com.example.gifty.DataSources.Users.UserDataSource

class UserRepository(private val userDataSource: UserDataSource) {
    suspend fun getUser(email: String, password: String): User? {
        return userDataSource.getUser(email, password)
    }
    suspend fun createUser(email: String, password: String): Boolean {
        return userDataSource.createUser(email, password)
    }
    suspend fun deleteUser(userId: Int): Boolean {
        return userDataSource.deleteUser(userId)
    }
    suspend fun updateUser(userId: Int, newPassword: String): Boolean {
        return userDataSource.updateUser(userId, newPassword)
    }
}

