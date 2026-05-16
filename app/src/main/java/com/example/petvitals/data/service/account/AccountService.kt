package com.example.petvitals.data.service.account

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.AccountError
import com.example.petvitals.domain.models.User
import kotlinx.coroutines.flow.Flow

interface AccountService {
    val currentUser: Flow<User?>
    val currentUserId: String
    val isEmailVerified: Boolean
    val currentUserEmail: String?
    fun hasUser(): Boolean
    suspend fun signIn(email: String, password: String): AppResult<AccountError, Unit>
    suspend fun signUp(email: String, password: String): AppResult<AccountError, String>
    suspend fun logout()
    suspend fun deleteAccount(): AppResult<AccountError, Unit>
    suspend fun sendVerificationEmail(): AppResult<AccountError, Unit>
    suspend fun sendPasswordResetEmail(email: String): AppResult<AccountError, Unit>
}
