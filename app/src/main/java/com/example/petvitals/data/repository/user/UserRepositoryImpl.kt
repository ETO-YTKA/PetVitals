package com.example.petvitals.data.repository.user

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor() : UserRepository {

    override suspend fun getCurrentUserData(userId: String): DocumentSnapshot {
        val userDoc = Firebase.firestore
            .collection("users")
            .document(userId)
            .get()
            .await()

        return userDoc
    }

    override suspend fun getUserDisplayName(userId: String): String {
        val userDoc = Firebase.firestore
            .collection("users")
            .document(userId)
            .get()
            .await()

        return userDoc.getString("displayName") ?: "anonymous"
    }

    override suspend fun createUserDocument(uid: String, displayName: String, email: String) {
        val userData = hashMapOf(
            "displayName" to displayName,
            "email" to email
        )

        Firebase.firestore.collection("users")
            .document(uid)
            .set(userData)
    }
}