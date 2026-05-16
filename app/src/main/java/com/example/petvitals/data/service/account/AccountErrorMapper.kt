package com.example.petvitals.data.service.account

import com.example.petvitals.domain.error.AccountError
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

internal fun Throwable.toAccountError(): AccountError = when (this) {
    is IllegalArgumentException -> AccountError.EmptyFields
    is FirebaseNetworkException -> AccountError.Network
    is FirebaseAuthInvalidCredentialsException -> AccountError.InvalidCredentials
    is FirebaseAuthInvalidUserException -> AccountError.UserNotFound
    is FirebaseAuthUserCollisionException -> AccountError.EmailAlreadyInUse
    is FirebaseAuthRecentLoginRequiredException -> AccountError.RequiresRecentLogin
    else -> AccountError.Unknown
}
