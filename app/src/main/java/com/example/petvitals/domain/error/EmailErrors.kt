package com.example.petvitals.domain.error

import com.example.petvitals.domain.AppError

enum class EmailErrors: AppError {
    EMPTY_FIELD,
    INVALID_EMAIL
}
