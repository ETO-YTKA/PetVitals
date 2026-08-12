package com.example.petvitals.domain.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.RecordOverview

interface GetCurrentUserRecords {
    suspend operator fun invoke(): AppResult<FirestoreError, List<RecordOverview>>
}
