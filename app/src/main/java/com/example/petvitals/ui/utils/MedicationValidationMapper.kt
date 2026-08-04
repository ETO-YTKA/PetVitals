package com.example.petvitals.ui.utils

import androidx.annotation.StringRes
import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.MedicationError

@StringRes
fun AppResult<MedicationError, Unit>.toMedicationMessageResOrNull(): Int? = when (this) {
    is AppResult.Success -> null
    is AppResult.Failure -> when (error) {
        MedicationError.BLANK_NAME -> R.string.medication_name_cannot_be_empty_error
        MedicationError.NAME_TOO_LONG -> R.string.medication_name_too_long_error
        MedicationError.BLANK_DOSAGE -> R.string.medication_dosage_cannot_be_empty_error
        MedicationError.DOSAGE_TOO_LONG -> R.string.medication_dosage_too_long_error
        MedicationError.BLANK_FREQUENCY -> R.string.medication_frequency_cannot_be_empty_error
        MedicationError.FREQUENCY_TOO_LONG -> R.string.medication_frequency_too_long_error
        MedicationError.START_DATE_REQUIRED ->
            R.string.medication_start_date_must_be_selected_error
        MedicationError.START_DATE_AFTER_END_DATE ->
            R.string.medication_start_date_cannot_be_after_end_date_error
        MedicationError.NOTE_TOO_LONG -> R.string.medication_note_too_long_error
    }
}