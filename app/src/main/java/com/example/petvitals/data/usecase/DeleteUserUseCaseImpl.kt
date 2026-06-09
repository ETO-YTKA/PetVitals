package com.example.petvitals.data.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.DeleteUserError
import com.example.petvitals.domain.usecase.DeleteUserUseCase

class DeleteUserUseCaseImpl : DeleteUserUseCase {
    override suspend fun invoke(userId: String): AppResult<DeleteUserError, Unit> {
        TODO("Not yet implemented")
    }
}