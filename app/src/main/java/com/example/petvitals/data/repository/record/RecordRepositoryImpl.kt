package com.example.petvitals.data.repository.record

import com.example.petvitals.data.repository.pet_permission.PetPermission
import com.example.petvitals.data.service.account.AccountService
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
            .collection("petPermissions")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .map { it.toObject<PetPermission>().petId }


        var accessedRecords: List<Record> = emptyList()
        if (petsId.isNotEmpty()) {
            accessedRecords = firestore
                .collection("records")
                .whereArrayContainsAny("petIds", petsId)
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

        val records = userRecords.minus(accessedRecords).plus(accessedRecords)
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
}