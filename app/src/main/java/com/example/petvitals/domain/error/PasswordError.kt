package com.example.petvitals.domain.error

import com.example.petvitals.domain.AppError

enum class PasswordError: AppError {
    EMPTY_FIELD,
    HAS_WHITESPACE,
    TOO_SHORT,
    NO_DIGIT,
    NO_UPPERCASE,
    NO_LOWERCASE
}
