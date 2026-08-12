package com.example.petvitals.domain.repository

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Record

interface RecordRepository {
    suspend fun saveRecord(
        record: Record,
        previousPetIds: List<String> = emptyList()
    ): AppResult<FirestoreError, Unit>
    suspend fun getCurrentUserRecords(petIds: List<String>): AppResult<FirestoreError, List<Record>>
    suspend fun deleteRecord(record: Record): AppResult<FirestoreError, Unit>
}
