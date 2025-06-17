package com.example.petvitals.data.repository.record

interface RecordRepository {
    suspend fun saveRecord(record: Record)
    suspend fun getRecordById(id: String): Record?
    suspend fun getAllRecord(): List<Record>
    suspend fun getRecordsByCondition(cond: String): List<Record>
    suspend fun deleteRecord(record: Record)
}