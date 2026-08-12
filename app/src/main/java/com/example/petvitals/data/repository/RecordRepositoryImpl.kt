package com.example.petvitals.data.repository

import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.data.utils.safeFirestoreCall
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.Record
import com.example.petvitals.domain.models.canManagePetCare
import com.example.petvitals.domain.repository.RecordRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecordRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val accountService: AccountService
) : RecordRepository {

    override suspend fun saveRecord(
        record: Record,
        previousPetIds: List<String>
    ): AppResult<FirestoreError, Unit> {
        val userId = accountService.currentUserId
            ?: return AppResult.Failure(FirestoreError.Unauthenticated)

        return safeFirestoreCall {
            val currentPetIds = record.petIds.distinct()
            val currentAnchor = currentPetIds.minOrNull()
                ?: throw IllegalArgumentException("A record must be linked to a pet")
            val previousAnchor = previousPetIds.distinct().minOrNull()

            firestore.runTransaction { transaction ->
                val previousRecord = previousAnchor?.let { anchorPetId ->
                    transaction
                        .get(recordReference(anchorPetId, record.id))
                        .toObject<Record>()
                        ?: throw recordTransactionException(
                            "Record does not exist",
                            FirebaseFirestoreException.Code.NOT_FOUND
                        )
                }

                if (previousRecord == null) {
                    val newRecordSnapshot = transaction.get(
                        recordReference(currentAnchor, record.id)
                    )
                    if (newRecordSnapshot.exists()) {
                        throw recordTransactionException(
                            "Record already exists",
                            FirebaseFirestoreException.Code.ALREADY_EXISTS
                        )
                    }
                } else if (previousRecord.revision != record.revision) {
                    throw recordTransactionException(
                        "Record was changed by another client",
                        FirebaseFirestoreException.Code.ABORTED
                    )
                }

                val authoritativePreviousPetIds = previousRecord
                    ?.petIds
                    .orEmpty()
                    .plus(previousAnchor)
                    .filterNotNull()
                val writePlan = createRecordWritePlan(
                    previousPetIds = authoritativePreviousPetIds,
                    currentPetIds = currentPetIds
                )
                requireManageAccess(
                    transaction = transaction,
                    petIds = writePlan.petIdsToDelete + writePlan.petIdsToSet,
                    userId = userId
                )
                val recordToSave = record.copy(
                    petIds = writePlan.petIdsToSet.toList(),
                    revision = (previousRecord?.revision ?: 0) + 1
                )

                writePlan.petIdsToDelete.forEach { petId ->
                    transaction.delete(recordReference(petId, record.id))
                }
                writePlan.petIdsToSet.forEach { petId ->
                    transaction.set(recordReference(petId, record.id), recordToSave)
                }
            }.await()
        }
    }

    override suspend fun getCurrentUserRecords(petIds: List<String>): AppResult<FirestoreError, List<Record>> {

        return safeFirestoreCall {
            coroutineScope {
                petIds
                    .distinct()
                    .map { petId ->
                        async {
                            firestore
                                .collection(FirestoreCollections.PETS)
                                .document(petId)
                                .collection(FirestoreCollections.RECORDS)
                                .get()
                                .await()
                                .mapNotNull { document ->
                                    document
                                        .toObject<Record>()
                                        .let { record ->
                                            record.copy(
                                                id = document.id,
                                                petIds = (record.petIds + petId).distinct()
                                            )
                                        }
                                }
                        }
                    }
                    .awaitAll()
                    .flatten()
                    .let(::mergeRecordCopies)
            }
        }
    }

    override suspend fun deleteRecord(record: Record): AppResult<FirestoreError, Unit> {
        val userId = accountService.currentUserId
            ?: return AppResult.Failure(FirestoreError.Unauthenticated)

        return safeFirestoreCall {
            val anchorPetId = record.petIds.distinct().minOrNull()
                ?: throw IllegalArgumentException("A record must be linked to a pet")

            firestore.runTransaction { transaction ->
                val storedRecord = transaction
                    .get(recordReference(anchorPetId, record.id))
                    .toObject<Record>()
                    ?: throw recordTransactionException(
                        "Record does not exist",
                        FirebaseFirestoreException.Code.NOT_FOUND
                    )

                if (storedRecord.revision != record.revision) {
                    throw recordTransactionException(
                        "Record was changed by another client",
                        FirebaseFirestoreException.Code.ABORTED
                    )
                }

                val authoritativePetIds = (storedRecord.petIds + anchorPetId).toSet()
                requireManageAccess(
                    transaction = transaction,
                    petIds = authoritativePetIds,
                    userId = userId
                )
                authoritativePetIds.forEach { petId ->
                    transaction.delete(recordReference(petId, record.id))
                }
            }.await()
        }
    }

    private fun recordReference(petId: String, recordId: String): DocumentReference =
        firestore
            .collection(FirestoreCollections.PETS)
            .document(petId)
            .collection(FirestoreCollections.RECORDS)
            .document(recordId)

    private fun recordTransactionException(
        message: String,
        code: FirebaseFirestoreException.Code
    ) = FirebaseFirestoreException(message, code)

    private fun requireManageAccess(
        transaction: Transaction,
        petIds: Set<String>,
        userId: String
    ) {
        petIds.forEach { petId ->
            val member = transaction
                .get(
                    firestore
                        .collection(FirestoreCollections.PETS)
                        .document(petId)
                        .collection(FirestoreCollections.PET_MEMBERS)
                        .document(userId)
                )
                .toObject<Member>()

            if (member?.permissionLevel?.canManagePetCare != true) {
                throw recordTransactionException(
                    "Record access denied",
                    FirebaseFirestoreException.Code.PERMISSION_DENIED
                )
            }
        }
    }
}

internal fun mergeRecordCopies(copies: List<Record>): List<Record> = copies
    .groupBy { it.id }
    .values
    .map { copiesForRecord ->
        copiesForRecord.maxBy { it.revision }.copy(
            petIds = copiesForRecord
                .flatMap { it.petIds }
                .distinct()
        )
    }
    .sortedByDescending { it.eventDate ?: it.createdAt }

internal data class RecordWritePlan(
    val petIdsToDelete: Set<String>,
    val petIdsToSet: Set<String>
)

internal fun createRecordWritePlan(
    previousPetIds: List<String>,
    currentPetIds: List<String>
): RecordWritePlan {
    val previous = previousPetIds.toSet()
    val current = currentPetIds.toSet()
    return RecordWritePlan(
        petIdsToDelete = previous - current,
        petIdsToSet = current
    )
}
