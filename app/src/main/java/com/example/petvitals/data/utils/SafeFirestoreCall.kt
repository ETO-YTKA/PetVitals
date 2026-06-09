package com.example.petvitals.data.utils

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import timber.log.Timber

suspend fun <T> safeFirestoreCall(
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