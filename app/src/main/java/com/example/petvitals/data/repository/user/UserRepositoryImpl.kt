package com.example.petvitals.data.repository.user

import com.example.petvitals.data.service.account.AccountService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val accountService: AccountService
) : UserRepository {

    override suspend fun createUser(user: User) {

        firestore
            .collection("users").document(user.id)
            .set(user)
    }

    override suspend fun getCurrentUser(): User {

        val userId = accountService.currentUserId
        val userDoc = firestore
            .collection("users")
            .document(userId)
            .get()
            .await()

        return User(
            id = userDoc["id"].toString(),
            username = userDoc["username"].toString(),
            email = userDoc["email"].toString()
        )
    }

    override suspend fun deleteCurrentUser() {

        val userId = accountService.currentUserId
        firestore
            .collection("users").document(userId)
            .delete()
            .await()
    }
}