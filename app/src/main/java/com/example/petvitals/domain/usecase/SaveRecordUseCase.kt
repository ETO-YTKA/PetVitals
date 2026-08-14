package com.example.petvitals.domain.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Record

interface SaveRecordUseCase {
    suspend operator fun invoke(
        record: Record,
        previousPetIds: List<String>
    ): AppResult<FirestoreError, Unit>
}