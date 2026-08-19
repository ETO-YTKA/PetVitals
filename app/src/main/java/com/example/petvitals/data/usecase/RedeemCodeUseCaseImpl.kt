package com.example.petvitals.data.usecase

import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.repository.PetInviteRepository
import com.example.petvitals.domain.repository.UserRepository
import com.example.petvitals.domain.usecase.RedeemCodeUseCase
import jakarta.inject.Inject

class RedeemCodeUseCaseImpl @Inject constructor(
    private val petInviteRepository: PetInviteRepository,
    private val userRepository: UserRepository,
    private val accountService: AccountService
) : RedeemCodeUseCase {
    override suspend fun invoke(code: String): AppResult<FirestoreError, Unit> {
        val currentUserId = accountService.currentUserId
            ?: return AppResult.Failure(FirestoreError.Unauthenticated)

        val user = when (val result = userRepository.getUserById(currentUserId)) {
            is AppResult.Success -> result.data ?: return AppResult.Failure(FirestoreError.Unauthenticated)
            is AppResult.Failure -> return result
        }

        return petInviteRepository.redeemCode(code, user)
    }
}