package com.example.petvitals.data.usecase

import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.repository.PetRepository
import com.example.petvitals.domain.usecase.CreatePetUseCase
import jakarta.inject.Inject

class CreatePetUseCaseImpl @Inject constructor(
    private val petRepository: PetRepository,
    private val accountService: AccountService
) : CreatePetUseCase {
    override suspend fun invoke(pet: Pet): AppResult<FirestoreError, Unit> {

        val userId = accountService.currentUserId
            ?: return AppResult.Failure(FirestoreError.Unauthenticated)

        val owner = Member(
            userId = userId,
            permissionLevel = PermissionLevel.OWNER
        )

        return petRepository.createPetWithOwner(pet, owner)
    }
}