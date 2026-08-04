package com.example.petvitals.domain.validator

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.MedicationError
import jakarta.inject.Inject

class MedicationDataValidator @Inject constructor() {

    fun validateName(name: String): AppResult<MedicationError, Unit> = when {
        name.isBlank() -> AppResult.Failure(MedicationError.BLANK_NAME)
        name.length > MAX_SHORT_TEXT_LENGTH -> AppResult.Failure(MedicationError.NAME_TOO_LONG)
        else -> AppResult.Success(Unit)
    }

    fun validateDosage(dosage: String): AppResult<MedicationError, Unit> = when {
        dosage.isBlank() -> AppResult.Failure(MedicationError.BLANK_DOSAGE)
        dosage.length > MAX_SHORT_TEXT_LENGTH -> AppResult.Failure(MedicationError.DOSAGE_TOO_LONG)
        else -> AppResult.Success(Unit)
    }

    fun validateFrequency(frequency: String): AppResult<MedicationError, Unit> = when {
        frequency.isBlank() -> AppResult.Failure(MedicationError.BLANK_FREQUENCY)
        frequency.length > MAX_SHORT_TEXT_LENGTH ->
            AppResult.Failure(MedicationError.FREQUENCY_TOO_LONG)
        else -> AppResult.Success(Unit)
    }

    fun validateSchedule(
        isRegular: Boolean,
        startDate: Long?,
        endDate: Long?
    ): AppResult<MedicationError, Unit> = when {
        isRegular -> AppResult.Success(Unit)
        startDate == null -> AppResult.Failure(MedicationError.START_DATE_REQUIRED)
        endDate != null && startDate > endDate ->
            AppResult.Failure(MedicationError.START_DATE_AFTER_END_DATE)
        else -> AppResult.Success(Unit)
    }

    fun validateNote(note: String): AppResult<MedicationError, Unit> = when {
        note.length > MAX_NOTE_LENGTH -> AppResult.Failure(MedicationError.NOTE_TOO_LONG)
        else -> AppResult.Success(Unit)
    }

    private companion object {
        const val MAX_SHORT_TEXT_LENGTH = 50
        const val MAX_NOTE_LENGTH = 500
    }
}