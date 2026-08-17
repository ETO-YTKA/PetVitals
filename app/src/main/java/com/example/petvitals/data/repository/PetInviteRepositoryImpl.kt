package com.example.petvitals.data.repository

import com.example.petvitals.data.utils.safeFirestoreCall
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PetInvite
import com.example.petvitals.domain.repository.PetInviteRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class PetInviteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PetInviteRepository {
    override suspend fun createCode(invite: PetInvite): AppResult<FirestoreError, Unit> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.INVITES)
                .document(invite.codeHash)
                .set(invite)
                .await()
        }
    }

    override suspend fun redeemCode(inviteId: String, member: Member): AppResult<FirestoreError, Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun revokeCode(inviteId: String): AppResult<FirestoreError, Unit> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.INVITES)
                .document(inviteId)
                .delete()
                .await()
        }
    }

    override suspend fun getCodes(petId: String): AppResult<FirestoreError, List<PetInvite>> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.INVITES)
                .whereEqualTo("petId", petId)
                .get()
                .await()
                .mapNotNull { doc ->
                    doc
                        .toObject<PetInvite>()
                        .copy(codeHash = doc.id)
                }
        }
    }
}