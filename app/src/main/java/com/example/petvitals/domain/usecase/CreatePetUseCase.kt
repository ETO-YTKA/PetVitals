package com.example.petvitals.domain.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Pet

interface CreatePetUseCase {
    suspend operator fun invoke(pet: Pet): AppResult<FirestoreError, Unit>
}