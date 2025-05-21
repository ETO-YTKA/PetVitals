package com.example.petvitals.data.service.account

import com.example.petvitals.data.repository.user.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountServiceImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AccountService {
    override val currentUser: Flow<User?>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                this.trySend(auth.currentUser?.let { User(it.uid) })
            }

            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    override val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    override fun hasUser(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun signUp(
        email: String,
        password: String
    ): String {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid
        return userId ?: throw IllegalStateException("Firebase returned null user after successful creation.")
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun deleteAccount() {
        auth.currentUser!!.delete().await()
    }
}