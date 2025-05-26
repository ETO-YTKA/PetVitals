package com.example.petvitals.data.repository.user

interface UserRepository {
    suspend fun createUser(user: User)
    suspend fun getCurrentUser(): User?
    suspend fun deleteCurrentUser()
}