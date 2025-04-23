package com.example.petvitals.model.service

import com.example.petvitals.model.User
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow

interface AccountService {
    val currentUser: Flow<User?>
    val currentUserId: String
    suspend fun getUserDisplayName(): String
    fun hasUser(): Boolean
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(name: String, email: String, password: String)
    suspend fun logout()
    suspend fun deleteAccount()
    suspend fun getCurrentUserData(): DocumentSnapshot
}