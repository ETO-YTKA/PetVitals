package com.example.petvitals.domain.error

import com.example.petvitals.domain.AppError

enum class RecordValidationError : AppError {
    TITLE_TOO_LONG,
    DESCRIPTION_TOO_LONG,
    PET_REQUIRED
}