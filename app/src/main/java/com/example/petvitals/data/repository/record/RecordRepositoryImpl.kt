package com.example.petvitals.data.repository.record

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
            .collection("users").document(record.userId)
            .collection("records").document(record.id)
            .set(record)
    }

    override suspend fun getRecordById(id: String): Record? {

        return firestore
            .collection("users").document(accountService.currentUserId)
            .collection("records").document(id)
            .get()
            .await()
            .toObject<Record>()
    }

    override suspend fun getAllRecord(): List<Record> {

        val userId = accountService.currentUserId
        val records = firestore
            .collection("users").document(userId)
            .collection("records")
            .get()
            .await()
            .sortedByDescending {
                it.getTimestamp("date")?.toDate()
            }

        return records.map { it.toObject<Record>() }
    }

    override suspend fun getRecordsByCondition(cond: String): List<Record> {

        val userId = accountService.currentUserId
        val records = firestore
            .collection("users").document(userId)
            .collection("records")
            .get()
            .await()
            .sortedByDescending {
                it.getTimestamp("date")?.toDate()
            }
            .map { it.toObject<Record>() }

        val filteredRecords = records.filter { record ->
            record.title.lowercase().contains(cond)
                || record.description.lowercase().contains(cond)
        }
        return filteredRecords
    }

    override suspend fun deleteRecord(record: Record) {

        firestore
            .collection("users").document(record.userId)
            .collection("records").document(record.id)
            .delete()
    }
}