package com.example.petvitals.ui.utils

import androidx.annotation.StringRes
import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FoodError

@StringRes
fun AppResult<FoodError, Unit>.toMessageResOrNull(): Int? {
    return when (this) {
        is AppResult.Success -> null
        is AppResult.Failure -> {
            when (error) {
                FoodError.BLANK_NAME -> R.string.food_name_cannot_be_empty_error
                FoodError.NAME_TOO_LONG -> R.string.food_name_too_long_error
                FoodError.BLANK_PORTION -> R.string.portion_cannot_be_empty_error
                FoodError.PORTION_TOO_LONG -> R.string.food_portion_too_long_error
                FoodError.BLANK_FREQUENCY -> R.string.frequency_cannot_be_empty_error
                FoodError.FREQUENCY_TOO_LONG -> R.string.food_frequency_too_long_error
                FoodError.NOTE_TOO_LONG -> R.string.food_note_too_long_error
            }
        }
    }
}
