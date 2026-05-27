package com.example.petvitals.domain.error

import com.example.petvitals.domain.AppError

sealed interface SignUpError : AppError {
    data object InvalidCredentials : SignUpError
    data object Unauthenticated : SignUpError
    data object EmailAlreadyInUse : SignUpError
    data object Network : SignUpError
    data object Unknown : SignUpError
}