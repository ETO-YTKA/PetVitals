package com.example.petvitals.domain.error

import com.example.petvitals.domain.AppError

sealed interface AccountError : AppError {
    data object EmptyFields : AccountError
    data object Network : AccountError
    data object InvalidCredentials : AccountError
    data object UserNotFound : AccountError
    data object EmailAlreadyInUse : AccountError
    data object NoAuthenticatedUser : AccountError
    data object RequiresRecentLogin : AccountError
    data object Unknown : AccountError
}
