package com.example.petvitals.data.service.account

import com.example.petvitals.data.repository.user.User
import kotlinx.coroutines.flow.Flow

interface AccountService {
    val currentUser: Flow<User?>
    val currentUserId: String
    val isEmailVerified: Boolean
    val currentUserEmail: String?
    fun hasUser(): Boolean
    suspend fun signIn(email: String, password: String): Boolean
    suspend fun signUp(email: String, password: String): String
    suspend fun logout()
    suspend fun deleteAccount()
    suspend fun sendVerificationEmail()
    suspend fun sendPasswordResetEmail(email: String)
}