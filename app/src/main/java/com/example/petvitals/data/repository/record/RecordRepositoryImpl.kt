package com.example.petvitals.data.repository.record

import com.example.petvitals.data.service.account.AccountService
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecordRepositoryImpl @Inject constructor(
    private val accountService: AccountService
) : RecordRepository {

    override suspend fun createRecord(record: Record) {

        Firebase.firestore
            .collection("users").document(record.userId)
            .collection("records").document(record.recordId)
            .set(record)
    }

    override suspend fun getRecordsByPetId(petId: String): List<Record> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllRecord(): List<Record> {

        val userId = accountService.currentUserId
        var records =
            Firebase.firestore
                .collection("users").document(userId)
                .collection("records")
                .get()
                .await()
                .sortedByDescending {
                    it.data["date"] as Long
                }

        return records.map {
            Record(
                recordId = it.id,
                userId = it.data["userId"] as String,
                title = it.data["title"] as String,
                type = enumValueOf<RecordType>(it.data["type"] as String),
                date = it.data["date"] as Long,
                description = it.data["description"] as String,
                petsId = it.data["petsId"] as List<String>
            )
        }
    }

    override suspend fun updateRecord(record: Record) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRecord(record: Record) {

        Firebase.firestore
            .collection("users").document(record.userId)
            .collection("records").document(record.recordId)
            .delete()
    }
}