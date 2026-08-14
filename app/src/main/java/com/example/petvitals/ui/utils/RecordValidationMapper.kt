package com.example.petvitals.ui.utils

import androidx.annotation.StringRes
import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.RecordValidationError

@StringRes
fun AppResult<RecordValidationError, Unit>.toRecordMessageResOrNull(): Int? = when (this) {
    is AppResult.Success -> null
    is AppResult.Failure -> when (error) {
        RecordValidationError.TITLE_TOO_LONG -> R.string.title_too_long
        RecordValidationError.DESCRIPTION_TOO_LONG ->
            R.string.description_cannot_be_longer_than_error
        RecordValidationError.PET_REQUIRED -> R.string.select_at_least_one_pet_error
    }
}
