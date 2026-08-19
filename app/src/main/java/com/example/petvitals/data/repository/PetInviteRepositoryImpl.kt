package com.example.petvitals.data.repository

import com.example.petvitals.data.utils.InviteCodeGenerator
import com.example.petvitals.data.utils.safeFirestoreCall
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PetInvite
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.repository.PetInviteRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class PetInviteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val inviteCodeGenerator: InviteCodeGenerator
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

    override suspend fun redeemCode(rawCode: String, user: User): AppResult<FirestoreError, Unit> {
        return safeFirestoreCall {
            firestore.runTransaction { transaction ->
                val codeHash = inviteCodeGenerator.hash(rawCode)

                val inviteRef = firestore
                    .collection(FirestoreCollections.INVITES)
                    .document(codeHash)

                val invite = transaction
                    .get(inviteRef)
                    .toObject<PetInvite>()

                if (invite == null) {
                    // TODO
                    return@runTransaction
                }

                val memberRef = firestore
                    .collection(FirestoreCollections.PETS)
                    .document(invite.petId)
                    .collection(FirestoreCollections.PET_MEMBERS)
                    .document(user.id)

                if (transaction.get(memberRef).exists()) {
                    //TODO
                    return@runTransaction
                }

                val member = Member(
                    userId = user.id,
                    displayName = user.username,
                    permissionLevel = invite.permissionLevel
                )
                transaction.set(memberRef, member)
            }.await()
        }
    }

    override suspend fun revokeCode(code: String): AppResult<FirestoreError, Unit> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.INVITES)
                .document(code)
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