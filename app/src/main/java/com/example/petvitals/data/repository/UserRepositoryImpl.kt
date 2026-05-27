package com.example.petvitals.data.repository

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun saveUser(user: User): AppResult<FirestoreError, Unit> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.USERS)
                .document(user.id)
                .set(user)
                .await()
        }
    }

    override suspend fun getUserById(userId: String): AppResult<FirestoreError, User?> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.USERS)
                .document(userId)
                .get()
                .await()
                .toObject<User>()
        }
    }

    override suspend fun getUserByEmail(email: String): AppResult<FirestoreError, User?> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.USERS)
                .whereEqualTo("email", email)
                .get()
                .await()
                .firstOrNull()
                ?.toObject<User>()
        }
    }

    override suspend fun deleteUser(userId: String): AppResult<FirestoreError, Unit> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.USERS)
                .document(userId)
                .delete()
                .await()
        }
    }

    private suspend inline fun <T> safeFirestoreCall(
        block: suspend () -> T
    ): AppResult<FirestoreError, T> {
        return try {
            val result = block()
            AppResult.Success(result)
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e)
            AppResult.Failure(e.toFirestoreError())
        } catch (e: Exception) {
            Timber.e(e)
            AppResult.Failure(FirestoreError.Unknown)
        }
    }
}

private fun FirebaseFirestoreException.toFirestoreError(): FirestoreError {
    return when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            FirestoreError.PermissionDenied

        FirebaseFirestoreException.Code.UNAUTHENTICATED ->
            FirestoreError.Unauthenticated

        FirebaseFirestoreException.Code.UNAVAILABLE,
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
            FirestoreError.Network

        else ->
            FirestoreError.Unknown
    }
}
