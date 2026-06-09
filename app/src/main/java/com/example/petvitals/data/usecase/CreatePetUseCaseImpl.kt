package com.example.petvitals.data.usecase

import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.repository.PetMemberRepository
import com.example.petvitals.domain.repository.PetRepository
import com.example.petvitals.domain.usecase.CreatePetUseCase
import jakarta.inject.Inject

class CreatePetUseCaseImpl @Inject constructor(
    private val petRepository: PetRepository,
    private val petMemberRepository: PetMemberRepository,
    private val accountService: AccountService
) : CreatePetUseCase {
    override suspend fun invoke(pet: Pet): AppResult<FirestoreError, Unit> {

        // Save pet
        when (val result = petRepository.savePet(pet)) {
            is AppResult.Success -> {}
            is AppResult.Failure -> return result
        }

        // Add pet member
        val userId = accountService.currentUserId ?: return AppResult.Failure(FirestoreError.Unauthenticated)
        val member = Member(userId = userId, PermissionLevel.OWNER)
        val saveMemberResult = petMemberRepository.savePetMember(
            petId = pet.id,
            member = member
        )

        when (saveMemberResult) {
            is AppResult.Success -> {}
            is AppResult.Failure -> return saveMemberResult
        }

        return AppResult.Success(Unit)
    }
}