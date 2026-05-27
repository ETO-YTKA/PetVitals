package com.example.petvitals.domain.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.SignUpError

interface SignUpUseCase {
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String
    ): AppResult<SignUpError, Unit>
}