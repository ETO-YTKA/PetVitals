package com.example.petvitals.domain.validator

import com.example.petvitals.domain.Result
import com.example.petvitals.domain.error.DisplayNameError
import com.example.petvitals.domain.error.EmailErrors
import com.example.petvitals.domain.error.PasswordError
import javax.inject.Inject

class UserDataValidator @Inject constructor() {

    fun validateDisplayName(displayName: String): Result<Unit, DisplayNameError> {
        val displayName = displayName.trim()

        if (displayName.isEmpty()) {
            return Result.Error(DisplayNameError.EMPTY_FIELD)
        }

        val regex = Regex("^[а-яА-Яa-zA-Z\\\\s'-]+\$")
        if (!regex.matches(displayName)) {
            return Result.Error(DisplayNameError.INVALID_CHARACTERS)
        }

        if (displayName.length > 50) {
            return Result.Error(DisplayNameError.TOO_LONG)
        }

        return Result.Success(Unit)
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

    fun validatePassword(password: String): Result<Unit, PasswordError> {

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
}