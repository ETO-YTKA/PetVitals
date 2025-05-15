package com.example.petvitals.data.repository.record

interface RecordRepository {
    suspend fun createRecord(record: Record)
    suspend fun getRecordsByPetId(petId: String): List<Record>
    suspend fun getAllRecord(userId: String): List<Record>
    suspend fun updateRecord(record: Record)
    suspend fun deleteRecord(record: Record)
}