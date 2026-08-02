package com.example.petvitals.domain.validator

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FoodError
import jakarta.inject.Inject

class FoodDataValidator @Inject constructor() {

    fun validateName(name: String): AppResult<FoodError, Unit> {

        if (name.isBlank()) {
            return AppResult.Failure(FoodError.BLANK_NAME)
        }

        if (name.length > 50) {
            return AppResult.Failure(FoodError.NAME_TOO_LONG)
        }

        return AppResult.Success(Unit)
    }

    fun validatePortion(portion: String): AppResult<FoodError, Unit> {

        if (portion.isBlank()) {
            return AppResult.Failure(FoodError.BLANK_PORTION)
        }

        if (portion.length > 50) {
            return AppResult.Failure(FoodError.PORTION_TOO_LONG)
        }

        return AppResult.Success(Unit)
    }

    fun validateFrequency(frequency: String): AppResult<FoodError, Unit> {

        if (frequency.isBlank()) {
            return AppResult.Failure(FoodError.BLANK_FREQUENCY)
        }

        if (frequency.length > 50) {
            return AppResult.Failure(FoodError.FREQUENCY_TOO_LONG)
        }

        return AppResult.Success(Unit)
    }

    fun validateNote(note: String): AppResult<FoodError, Unit> {

        if (note.length > 500) {
            return AppResult.Failure(FoodError.NOTE_TOO_LONG)
        }

        return AppResult.Success(Unit)
    }
}