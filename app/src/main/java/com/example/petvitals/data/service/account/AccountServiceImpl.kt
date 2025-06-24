package com.example.petvitals.data.service.account

import android.content.Context
import android.widget.Toast
import com.example.petvitals.R
import com.example.petvitals.data.repository.user.User
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountServiceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
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

    override val isEmailVerified: Boolean
        get() = auth.currentUser?.isEmailVerified ?: false

    override val currentUserEmail: String?
        get() = auth.currentUser?.email

    override fun hasUser(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun signIn(email: String, password: String): Boolean {
        val authRes = auth.signInWithEmailAndPassword(email, password).await()

        val user = authRes.user
        return user != null && user.isEmailVerified
    }

    override suspend fun signUp(
        email: String,
        password: String
    ): String {
        val result = auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.email_sent, user.email),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.failed_to_send_email),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.failed_to_create_account),
                        Toast.LENGTH_LONG
                    ).show()
                }
        }.await()

        return result.user?.uid ?: throw Exception(context.getString(R.string.failed_to_create_account))
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun deleteAccount() {
        auth.currentUser!!.delete().await()
    }

    override suspend fun sendVerificationEmail() {
        auth.currentUser?.sendEmailVerification()?.await()
    }

    override suspend fun sendPasswordResetEmail(email: String) {

        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(
                    context,
                    context.getString(R.string.password_reset_email_sent),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.failed_to_send_password_reset_email),
                    Toast.LENGTH_LONG
                ).show()
            }
        }.await()
    }
}