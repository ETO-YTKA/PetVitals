package com.example.petvitals.domain.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError

interface RedeemCodeUseCase {
    suspend operator fun invoke(code: String): AppResult<FirestoreError, Unit>
}