package com.example.petvitals.domain.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.DeleteUserError

interface DeleteUserUseCase {
    suspend operator fun invoke(userId: String): AppResult<DeleteUserError, Unit>
}