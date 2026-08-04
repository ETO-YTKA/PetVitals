package com.example.petvitals.ui.utils

import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.MedicationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MedicationValidationMapperTest {

    @Test
    fun mapper_returnsExpectedResources() {
        val expected = mapOf(
            MedicationError.BLANK_NAME to R.string.medication_name_cannot_be_empty_error,
            MedicationError.NAME_TOO_LONG to R.string.medication_name_too_long_error,
            MedicationError.BLANK_DOSAGE to R.string.medication_dosage_cannot_be_empty_error,
            MedicationError.DOSAGE_TOO_LONG to R.string.medication_dosage_too_long_error,
            MedicationError.BLANK_FREQUENCY to R.string.medication_frequency_cannot_be_empty_error,
            MedicationError.FREQUENCY_TOO_LONG to R.string.medication_frequency_too_long_error,
            MedicationError.START_DATE_REQUIRED to R.string.medication_start_date_must_be_selected_error,
            MedicationError.START_DATE_AFTER_END_DATE to
                    R.string.medication_start_date_cannot_be_after_end_date_error,
            MedicationError.NOTE_TOO_LONG to R.string.medication_note_too_long_error
        )

        expected.forEach { (error, messageRes) ->
            assertEquals(
                messageRes,
                AppResult.Failure(error).toMedicationMessageResOrNull()
            )
        }
        assertNull(AppResult.Success(Unit).toMedicationMessageResOrNull())
    }
}
