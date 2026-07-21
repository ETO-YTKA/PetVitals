package com.example.petvitals.data.usecase

import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.repository.PetMemberRepository
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import jakarta.inject.Inject

class GetPetPermissionUseCaseImpl @Inject constructor(
    private val petMemberRepository: PetMemberRepository,
    private val accountService: AccountService
) : GetPetPermissionUseCase {

    override suspend fun invoke(petId: String): AppResult<FirestoreError, PermissionLevel> {
        val userId = accountService.currentUserId
            ?: return AppResult.Failure(FirestoreError.Unauthenticated)

        return when (val result = petMemberRepository.getPetRole(petId, userId)) {
            is AppResult.Success -> result.data
                ?.let { AppResult.Success(it) }
                ?: AppResult.Failure(FirestoreError.PermissionDenied)
            is AppResult.Failure -> result
        }
    }
}
