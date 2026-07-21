package com.example.petvitals.domain.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.PermissionLevel

interface GetPetPermissionUseCase {
    suspend operator fun invoke(petId: String): AppResult<FirestoreError, PermissionLevel>
}
