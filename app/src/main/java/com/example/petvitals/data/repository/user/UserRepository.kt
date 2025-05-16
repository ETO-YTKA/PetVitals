package com.example.petvitals.data.repository.user

interface UserRepository {
    suspend fun createUserDocument(user: User)
    suspend fun getCurrentUser(): User
    suspend fun deleteCurrentUser()
}