package com.example.petvitals.domain

import javax.inject.Inject

class SignUpDataValidator @Inject constructor() {

    fun validateDisplayName(displayName: String): Result<Unit, DisplayNameError> {
        val displayName = displayName.trim()

        if (displayName.isEmpty()) {
            return Result.Error(DisplayNameError.EMPTY_FIELD)
        }

        if (displayName.length > 30) {
            return Result.Error(DisplayNameError.TOO_LONG)
        }

        return Result.Success(Unit)
    }

    enum class DisplayNameError: Error {
        EMPTY_FIELD,
        TOO_LONG
    }

    fun validateEmail(email: String): Result<Unit, EmailErrors> {
        val email = email.trim()

        if (email.isEmpty()) {
            return Result.Error(EmailErrors.EMPTY_FIELD)
        }

        val emailRegex = Regex("^((?!\\.)[\\w\\-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])\$")
        if (!emailRegex.matches(email)) {
            return Result.Error(EmailErrors.INVALID_EMAIL)
        }

        return Result.Success(Unit)
    }

    enum class EmailErrors: Error {
        EMPTY_FIELD,
        INVALID_EMAIL
    }

    fun validatePassword(password: String): Result<Unit, PasswordError> {

        val password = password.trim()

        if (password.isEmpty()) {
            return Result.Error(PasswordError.EMPTY_FIELD)
        }

        if (password.contains(' ')) {
            return Result.Error(PasswordError.HAS_WHITESPACE)
        }

        if (password.length < 8) {
            return Result.Error(PasswordError.TOO_SHORT)
        }

        val hasDigit = password.any { it.isDigit() }
        if (!hasDigit) {
            return Result.Error(PasswordError.NO_DIGIT)
        }

        val hasUpperCase = password.any { it.isUpperCase() }
        if (!hasUpperCase) {
            return Result.Error(PasswordError.NO_UPPERCASE)
        }

        val hasLowerCase = password.any { it.isLowerCase() }
        if (!hasLowerCase) {
            return Result.Error(PasswordError.NO_LOWERCASE)
        }

        return Result.Success(Unit)
    }

    enum class PasswordError: Error {
        EMPTY_FIELD,
        HAS_WHITESPACE,
        TOO_SHORT,
        NO_DIGIT,
        NO_UPPERCASE,
        NO_LOWERCASE
    }
}