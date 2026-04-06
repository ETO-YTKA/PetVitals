package com.example.petvitals.domain.repository

import com.example.petvitals.domain.models.Record

interface RecordRepository {
    suspend fun saveRecord(record: Record)
    suspend fun getRecordById(id: String): Record?
    suspend fun getCurrentUserRecords(): List<Record>
    suspend fun deleteRecord(record: Record)
}