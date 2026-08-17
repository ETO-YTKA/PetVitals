package com.example.petvitals.data.usecase

import com.example.petvitals.data.utils.InviteCodeGenerator
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.CreatedPetInvite
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.PetInvite
import com.example.petvitals.domain.repository.PetInviteRepository
import com.example.petvitals.domain.usecase.CreateInviteCodeUseCase
import jakarta.inject.Inject

class CreateInviteCodeUseCaseImpl @Inject constructor(
    private val petInviteRepository: PetInviteRepository,
    private val inviteCodeGenerator: InviteCodeGenerator
) : CreateInviteCodeUseCase {
    override suspend fun invoke(
        petId: String,
        permissionLevel: PermissionLevel
    ): AppResult<FirestoreError, CreatedPetInvite> {

        val rawCode = inviteCodeGenerator.generate()
        val normalizedCode = inviteCodeGenerator.normalize(rawCode)
        val codeHash = inviteCodeGenerator.hash(normalizedCode)
        val petInvite = PetInvite(
            codeHash = codeHash,
            petId = petId,
            permissionLevel = permissionLevel,
        )

        return when (val result = petInviteRepository.createCode(petInvite)) {
            is AppResult.Failure -> result
            is AppResult.Success -> {
                AppResult.Success(CreatedPetInvite(rawCode, petInvite))
            }
        }
    }
}