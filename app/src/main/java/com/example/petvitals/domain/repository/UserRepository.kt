package com.example.petvitals.domain.repository

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.User

interface UserRepository {
    suspend fun saveUser(user: User): AppResult<FirestoreError, Unit>
    suspend fun getUserById(userId: String): AppResult<FirestoreError, User?>
    suspend fun getUserByEmail(email: String): AppResult<FirestoreError, User?>
    suspend fun deleteUser(userId: String): AppResult<FirestoreError, Unit>
}