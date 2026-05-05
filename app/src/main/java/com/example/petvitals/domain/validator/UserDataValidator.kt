package com.example.petvitals.domain.validator

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.EmailErrors
import com.example.petvitals.domain.error.NameError
import com.example.petvitals.domain.error.PasswordError
import javax.inject.Inject

class UserDataValidator @Inject constructor() {

    fun validateName(name: String): AppResult<NameError, Unit> {
        val name = name.trim()

        if (name.isEmpty()) {
            return AppResult.Failure(NameError.EMPTY_FIELD)
        }

        val regex = Regex("^[а-яА-Яa-zA-Z\\\\s'-]+\$")
        if (!regex.matches(name)) {
            return AppResult.Failure(NameError.INVALID_CHARACTERS)
        }

        if (name.length > 50) {
            return AppResult.Failure(NameError.TOO_LONG)
        }

        return AppResult.Success(Unit)
    }

    fun validateEmail(email: String): AppResult<EmailErrors, Unit> {
        val email = email.trim()

        if (email.isEmpty()) {
            return AppResult.Failure(EmailErrors.EMPTY_FIELD)
        }

        val emailRegex = Regex("^((?!\\.)[\\w\\-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])\$")
        if (!emailRegex.matches(email)) {
            return AppResult.Failure(EmailErrors.INVALID_EMAIL)
        }

        return AppResult.Success(Unit)
    }

    fun validatePassword(password: String): AppResult<PasswordError, Unit> {

        if (password.isEmpty()) {
            return AppResult.Failure(PasswordError.EMPTY_FIELD)
        }

        if (password.contains(' ')) {
            return AppResult.Failure(PasswordError.HAS_WHITESPACE)
        }

        if (password.length < 8) {
            return AppResult.Failure(PasswordError.TOO_SHORT)
        }

        val hasDigit = password.any { it.isDigit() }
        if (!hasDigit) {
            return AppResult.Failure(PasswordError.NO_DIGIT)
        }

        val hasUpperCase = password.any { it.isUpperCase() }
        if (!hasUpperCase) {
            return AppResult.Failure(PasswordError.NO_UPPERCASE)
        }

        val hasLowerCase = password.any { it.isLowerCase() }
        if (!hasLowerCase) {
            return AppResult.Failure(PasswordError.NO_LOWERCASE)
        }

        return AppResult.Success(Unit)
    }
}