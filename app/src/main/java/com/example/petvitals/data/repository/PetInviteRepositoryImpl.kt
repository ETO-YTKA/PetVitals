package com.example.petvitals.data.repository

import com.example.petvitals.data.utils.InviteCodeGenerator
import com.example.petvitals.data.utils.safeFirestoreCall
import com.example.petvitals.data.utils.toFirestoreError
import com.example.petvitals.data.utils.toPetInviteError
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.PetInviteError
import com.example.petvitals.domain.mapError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PetInvite
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.repository.PetInviteRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.toObject
import jakarta.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class PetInviteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val inviteCodeGenerator: InviteCodeGenerator
) : PetInviteRepository {
    override suspend fun createCode(invite: PetInvite): AppResult<PetInviteError, Unit> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.INVITES)
                .document(invite.codeHash)
                .set(invite)
                .await()
            Unit
        }.mapError { it.toPetInviteError() }
    }

    override suspend fun redeemCode(
        rawCode: String,
        user: User
    ): AppResult<PetInviteError, Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val codeHash = inviteCodeGenerator.hash(rawCode)

                val inviteRef = firestore
                    .collection(FirestoreCollections.INVITES)
                    .document(codeHash)

                val invite = transaction
                    .get(inviteRef)
                    .toObject<PetInvite>()

                if (invite == null) {
                    throw InviteUnavailableException()
                }

                val memberRef = firestore
                    .collection(FirestoreCollections.PETS)
                    .document(invite.petId)
                    .collection(FirestoreCollections.PET_MEMBERS)
                    .document(user.id)

                if (transaction.get(memberRef).exists()) {
                    throw AlreadyMemberException()
                }

                val member = Member(
                    userId = user.id,
                    displayName = user.username,
                    permissionLevel = invite.permissionLevel
                )
                transaction.set(memberRef, member)
                transaction.delete(inviteRef)
            }.await()
            AppResult.Success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: InviteUnavailableException) {
            AppResult.Failure(PetInviteError.InviteUnavailable)
        } catch (_: AlreadyMemberException) {
            AppResult.Failure(PetInviteError.AlreadyMember)
        } catch (exception: FirebaseFirestoreException) {
            AppResult.Failure(exception.toFirestoreError().toPetInviteError())
        } catch (_: Exception) {
            AppResult.Failure(PetInviteError.Unknown)
        }
    }

    override suspend fun revokeCode(code: String): AppResult<PetInviteError, Unit> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.INVITES)
                .document(code)
                .delete()
                .await()
            Unit
        }.mapError { it.toPetInviteError() }
    }

    override suspend fun getCodes(petId: String): AppResult<PetInviteError, List<PetInvite>> {

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
        }.mapError { it.toPetInviteError() }
    }
}

private class InviteUnavailableException : Exception()
private class AlreadyMemberException : Exception()