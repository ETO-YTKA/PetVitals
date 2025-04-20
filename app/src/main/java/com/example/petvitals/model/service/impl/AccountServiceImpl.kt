package com.example.petvitals.model.service.impl

import android.util.Log
import com.example.petvitals.model.User
import com.example.petvitals.model.service.AccountService
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountServiceImpl @Inject constructor() : AccountService {
    override val currentUser: Flow<User?>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                this.trySend(auth.currentUser?.let { User(it.uid) })
            }

            Firebase.auth.addAuthStateListener(listener)
            awaitClose { Firebase.auth.removeAuthStateListener(listener) }
        }

    override val currentUserId: String
        get() = Firebase.auth.currentUser?.uid.orEmpty()

    override suspend fun currentUserName(): String {
        val userId = currentUserId

        if (userId.isEmpty()) {
            return "anonymous"
        }

        val userDoc = Firebase.firestore
            .collection("users")
            .document(userId)
            .get()
            .await()

        return userDoc.getString("displayName") ?: "anonymous"
    }

    override fun hasUser(): Boolean {
        return Firebase.auth.currentUser != null
    }

    override suspend fun signIn(email: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun signUp(name: String, email: String, password: String) {
            val authResult = Firebase.auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user == null) {
                Log.d("SignUpError", "Firebase Authentication user object is null after creation.")
            } else {
                val userData = hashMapOf(
                    "displayName" to name
                )

                Firebase.firestore.collection("users")
                    .document(user.uid)
                    .set(userData)
            }
    }

    override suspend fun signOut() {
        Firebase.auth.signOut()
    }

    override suspend fun deleteAccount() {
        Firebase.auth.currentUser!!.delete().await()
    }
}