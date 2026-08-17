package com.example.petvitals.data.usecase

import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.repository.PetRepository
import com.example.petvitals.domain.repository.UserRepository
import com.example.petvitals.domain.usecase.CreatePetUseCase
import jakarta.inject.Inject

class CreatePetUseCaseImpl @Inject constructor(
    private val petRepository: PetRepository,
    private val userRepository: UserRepository,
    private val accountService: AccountService
) : CreatePetUseCase {
    override suspend fun invoke(pet: Pet): AppResult<FirestoreError, Unit> {

        val userId = accountService.currentUserId
            ?: return AppResult.Failure(FirestoreError.Unauthenticated)
        val user = when (val result = userRepository.getUserById(userId)) {
            is AppResult.Success -> result.data ?: return AppResult.Failure(FirestoreError.Unauthenticated)
            is AppResult.Failure -> return result
        }

        val owner = Member(
            userId = userId,
            displayName = user.username,
            permissionLevel = PermissionLevel.OWNER
        )

        return petRepository.createPetWithOwner(pet, owner)
    }
}