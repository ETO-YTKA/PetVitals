package com.example.petvitals.domain.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Record

interface DeleteRecordUseCase {
    suspend operator fun invoke(record: Record): AppResult<FirestoreError, Unit>
}
