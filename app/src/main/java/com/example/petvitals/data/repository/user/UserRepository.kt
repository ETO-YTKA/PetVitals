package com.example.petvitals.data.repository.user

interface UserRepository {
    suspend fun saveUser(user: User)
    suspend fun getCurrentUser(): User?
    suspend fun getUserById(userId: String): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun deleteCurrentUser()
}