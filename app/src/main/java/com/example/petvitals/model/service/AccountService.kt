package com.example.petvitals.model.service

import com.example.petvitals.model.User
import kotlinx.coroutines.flow.Flow

interface AccountService {
    val currentUser: Flow<User?>
    val currentUserId: String
    suspend fun currentUserName(): String
    fun hasUser(): Boolean
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(name: String, email: String, password: String)
    suspend fun signOut()
    suspend fun deleteAccount()
}