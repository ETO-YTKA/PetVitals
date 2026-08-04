package com.example.petvitals.domain.error

import com.example.petvitals.domain.AppError

enum class MedicationError : AppError {
    BLANK_NAME,
    NAME_TOO_LONG,
    BLANK_DOSAGE,
    DOSAGE_TOO_LONG,
    BLANK_FREQUENCY,
    FREQUENCY_TOO_LONG,
    START_DATE_REQUIRED,
    START_DATE_AFTER_END_DATE,
    NOTE_TOO_LONG
}