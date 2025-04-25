package com.example.petvitals.data.repository.user

import com.google.firebase.firestore.DocumentSnapshot

interface UserRepository {
    suspend fun getUserDisplayName(userId: String): String
    suspend fun getCurrentUserData(userId: String): DocumentSnapshot
    suspend fun createUserDocument(uid: String, displayName: String, email: String)
}