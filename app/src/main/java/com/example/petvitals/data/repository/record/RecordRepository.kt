package com.example.petvitals.data.repository.record

interface RecordRepository {
    suspend fun saveRecord(record: Record)
    suspend fun getRecordById(id: String): Record?
    suspend fun getCurrentUserRecords(searchQuery: String = ""): List<Record>
    suspend fun deleteRecord(record: Record)
}