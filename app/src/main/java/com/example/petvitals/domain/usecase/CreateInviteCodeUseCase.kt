package com.example.petvitals.domain.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.PetInviteError
import com.example.petvitals.domain.models.CreatedPetInvite
import com.example.petvitals.domain.models.PermissionLevel

interface CreateInviteCodeUseCase {
    suspend operator fun invoke(
        petId: String,
        permissionLevel: PermissionLevel
    ): AppResult<PetInviteError, CreatedPetInvite>
}