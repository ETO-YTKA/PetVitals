package com.example.petvitals.domain.repository

import com.example.petvitals.domain.models.User

interface UserRepository {
    suspend fun saveUser(user: User)
    suspend fun getCurrentUser(): User?
    suspend fun getUserById(userId: String): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun deleteCurrentUser()
}