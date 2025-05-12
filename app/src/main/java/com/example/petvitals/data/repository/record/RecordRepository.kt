package com.example.petvitals.data.repository.record

interface RecordRepository {
    suspend fun createUserRecord(record: Record)
    suspend fun getRecordByPetId(petId: String): List<Record>
    suspend fun getAllRecord(): List<Record>
    suspend fun getRecordById(recordId: String): Record?
    suspend fun updateRecord(record: Record)
    suspend fun deleteRecord(recordId: String)
}