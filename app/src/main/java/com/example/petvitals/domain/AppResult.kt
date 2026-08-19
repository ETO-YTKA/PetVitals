package com.example.petvitals.domain

interface AppError

sealed interface AppResult<out E : AppError, out D> {
    data class Success<out D>(val data: D) : AppResult<Nothing, D>
    data class Failure<out E : AppError>(val error: E) : AppResult<E, Nothing>
}

inline fun <E : AppError, R : AppError, D> AppResult<E, D>.mapError(
    transform: (E) -> R
): AppResult<R, D> = when (this) {
    is AppResult.Success -> this
    is AppResult.Failure -> AppResult.Failure(transform(error))
}