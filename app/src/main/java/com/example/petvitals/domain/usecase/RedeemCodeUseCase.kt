package com.example.petvitals.domain.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.PetInviteError

interface RedeemCodeUseCase {
    suspend operator fun invoke(code: String): AppResult<PetInviteError, Unit>
}