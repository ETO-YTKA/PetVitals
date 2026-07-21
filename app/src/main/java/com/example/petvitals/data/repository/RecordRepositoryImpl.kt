package com.example.petvitals.data.repository

import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.models.Record
import com.example.petvitals.domain.repository.RecordRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecordRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val accountService: AccountService
) : RecordRepository {

    override suspend fun saveRecord(record: Record) {

        firestore
            .collection("records").document(record.id)
            .set(record)
    }

    override suspend fun getRecordById(id: String): Record? {

        return firestore
            .collection("records").document(id)
            .get()
            .await()
            .toObject<Record>()
    }

    override suspend fun getCurrentUserRecords(): List<Record> {

        val userId = accountService.currentUserId

        val petsId = firestore
            .collectionGroup(FirestoreCollections.PET_MEMBERS)
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .mapNotNull { memberDocument ->
                val petDocument = memberDocument.reference.parent.parent
                    ?: return@mapNotNull null
                petDocument
                    .get()
                    .await()
                    .takeIf { it.exists() }
                    ?.id
            }
            .distinct()


        val accessedRecords = petsId
            .chunked(MAX_WHERE_ARRAY_CONTAINS_ANY_VALUES)
            .flatMap { petIdChunk ->
                firestore
                    .collection("records")
                    .whereArrayContainsAny("petIds", petIdChunk)
                    .get()
                    .await()
                    .map { it.toObject<Record>() }
            }

        val userRecords = firestore
            .collection("records")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .map { it.toObject<Record>() }

        val records = (userRecords + accessedRecords)
            .distinctBy { it.id }
            .sortedByDescending {
                it.date
            }

        return records
    }

    override suspend fun deleteRecord(record: Record) {

        firestore
            .collection("records").document(record.id)
            .delete()
    }

    private companion object {
        const val MAX_WHERE_ARRAY_CONTAINS_ANY_VALUES = 30
    }
}
