package com.example.petvitals.domain.error

import com.example.petvitals.domain.AppError

enum class NameError: AppError {
    EMPTY_FIELD,
    TOO_LONG,
    INVALID_CHARACTERS
}
