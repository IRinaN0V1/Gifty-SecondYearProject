package com.example.gifty.Interactors

import com.example.gifty.Repositories.UserRepository
import com.example.gifty.Data.User

class UserInteractor(private val userRepository: UserRepository) {
    suspend fun getUser(email: String, password: String): User? {
        return userRepository.getUser(email, password)
    }
    suspend fun createUser(email: String, password: String): Boolean {
        return userRepository.createUser(email, password)
    }
    suspend fun deleteUser(userId: Int): Boolean {
        return userRepository.deleteUser(userId)
    }
    suspend fun updateUser(userId: Int, newPassword: String): Boolean {
        return userRepository.updateUser(userId, newPassword)
    }
    fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && email.contains("@") && email.contains(".")
    }

    fun validatePassword(password: String): Boolean {
        val regex = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)".toRegex()
        return password.length >= 6 && regex.containsMatchIn(password)
    }

    fun passwordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }
}