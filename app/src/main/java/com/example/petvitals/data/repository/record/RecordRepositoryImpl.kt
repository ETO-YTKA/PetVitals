package com.example.petvitals.data.repository.record

import com.example.petvitals.data.service.account.AccountService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecordRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val accountService: AccountService
) : RecordRepository {

    override suspend fun createRecord(record: Record) {

        firestore
            .collection("users").document(record.userId)
            .collection("records").document(record.id)
            .set(record)
    }

    override suspend fun getRecordsByPetId(petId: String): List<Record> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllRecord(): List<Record> {

        val userId = accountService.currentUserId
        var records = firestore
            .collection("users").document(userId)
            .collection("records")
            .get()
            .await()
            .sortedByDescending {
                it.data["date"] as Long
            }

        return records.map { it.toObject<Record>() }
    }

    override suspend fun getRecordsByCondition(cond: String): List<Record> {

        val userId = accountService.currentUserId
        var records = firestore
            .collection("users").document(userId)
            .collection("records")
            .get()
            .await()
            .toObjects<Record>()

        val filteredRecords = records.filter { record ->
            record.title.contains(cond)
                || record.description.contains(cond)
                || record.petsName.any { it.contains(cond) }

        }
        return filteredRecords
    }

    override suspend fun updateRecord(record: Record) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRecord(record: Record) {

        firestore
            .collection("users").document(record.userId)
            .collection("records").document(record.id)
            .delete()
    }
}