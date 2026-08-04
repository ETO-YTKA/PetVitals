package com.example.petvitals.domain.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Medication

interface SaveMedicationUseCase {
    suspend operator fun invoke(
        medication: Medication
    ): AppResult<FirestoreError, Unit>
}
