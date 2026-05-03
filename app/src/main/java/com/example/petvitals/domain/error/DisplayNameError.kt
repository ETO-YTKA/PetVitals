package com.example.petvitals.domain.error

import com.example.petvitals.domain.AppError

enum class DisplayNameError: AppError {
    EMPTY_FIELD,
    TOO_LONG,
    INVALID_CHARACTERS
}
