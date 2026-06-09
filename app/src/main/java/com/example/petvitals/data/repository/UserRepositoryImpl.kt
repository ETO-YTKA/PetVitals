package com.example.petvitals.data.repository

import com.example.petvitals.data.utils.safeFirestoreCall
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
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
}
