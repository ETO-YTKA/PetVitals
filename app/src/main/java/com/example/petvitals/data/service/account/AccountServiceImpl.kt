package com.example.petvitals.data.service.account

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.AccountError
import com.example.petvitals.domain.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
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

    override val isEmailVerified: Boolean
        get() = auth.currentUser?.isEmailVerified ?: false

    override val currentUserEmail: String?
        get() = auth.currentUser?.email

    override fun hasUser(): Boolean = auth.currentUser != null

    override suspend fun signIn(email: String, password: String): AppResult<AccountError, Unit> {
        return accountResult {
            auth.signInWithEmailAndPassword(email, password).await()
        }
    }

    override suspend fun signUp(
        email: String,
        password: String
    ): AppResult<AccountError, String> {
        return accountResult {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw IllegalStateException("Created account has no Firebase user")

            user.sendEmailVerification().await()

            user.uid
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun deleteAccount(): AppResult<AccountError, Unit> {
        val user = auth.currentUser ?: return AppResult.Failure(AccountError.NoAuthenticatedUser)

        return accountResult {
            user.delete().await()
        }
    }

    override suspend fun sendVerificationEmail(): AppResult<AccountError, Unit> {
        val user = auth.currentUser ?: return AppResult.Failure(AccountError.NoAuthenticatedUser)

        return accountResult {
            user.sendEmailVerification().await()
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AppResult<AccountError, Unit> {
        return accountResult {
            auth.sendPasswordResetEmail(email).await()
        }
    }

    private suspend fun <D> accountResult(block: suspend () -> D): AppResult<AccountError, D> {
        return try {
            AppResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e("Account operation failed: $e")
            AppResult.Failure(e.toAccountError())
        }
    }
}
