package com.example.petvitals.data.repository.record

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class RecordRepositoryImpl @Inject constructor() : RecordRepository {

    override suspend fun createUserRecord(record: Record) {
        Firebase.firestore.collection("users").document(record.userId)
            .collection("records").add(record)
    }

    override suspend fun getRecordByPetId(petId: String): List<Record> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllRecord(): List<Record> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecordById(recordId: String): Record? {
        TODO("Not yet implemented")
    }

    override suspend fun updateRecord(record: Record) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRecord(recordId: String) {
        TODO("Not yet implemented")
    }
}