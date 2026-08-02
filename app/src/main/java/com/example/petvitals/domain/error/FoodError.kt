package com.example.petvitals.domain.error

import com.example.petvitals.domain.AppError

enum class FoodError: AppError {
    BLANK_NAME,
    NAME_TOO_LONG,
    BLANK_PORTION,
    PORTION_TOO_LONG,
    BLANK_FREQUENCY,
    FREQUENCY_TOO_LONG,
    NOTE_TOO_LONG
}