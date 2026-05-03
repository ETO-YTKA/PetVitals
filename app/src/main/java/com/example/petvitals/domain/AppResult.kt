package com.example.petvitals.domain

interface AppError

sealed interface AppResult<out E : AppError, out D> {
    data class Success<out D>(val data: D) : AppResult<Nothing, D>
    data class Failure<out E : AppError>(val error: E) : AppResult<E, Nothing>
}
