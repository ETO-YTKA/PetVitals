package com.example.petvitals.domain.validator

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.MedicationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationDataValidatorTest {

    private val validator = MedicationDataValidator()

    @Test
    fun validateTextFields_returnsExpectedErrors() {
        assertFailure(validator.validateName(""), MedicationError.BLANK_NAME)
        assertFailure(validator.validateName("x".repeat(51)), MedicationError.NAME_TOO_LONG)
        assertFailure(validator.validateDosage(""), MedicationError.BLANK_DOSAGE)
        assertFailure(validator.validateDosage("x".repeat(51)), MedicationError.DOSAGE_TOO_LONG)
        assertFailure(validator.validateFrequency(""), MedicationError.BLANK_FREQUENCY)
        assertFailure(
            validator.validateFrequency("x".repeat(51)),
            MedicationError.FREQUENCY_TOO_LONG
        )
        assertFailure(validator.validateNote("x".repeat(501)), MedicationError.NOTE_TOO_LONG)
    }

    @Test
    fun validateSchedule_whenRegular_acceptsMissingDates() {
        assertTrue(
            validator.validateSchedule(
                isRegular = true,
                startDate = null,
                endDate = null
            ) is AppResult.Success<*>
        )
    }

    @Test
    fun validateSchedule_whenSpecific_requiresStartDate() {
        assertFailure(
            validator.validateSchedule(
                isRegular = false,
                startDate = null,
                endDate = null
            ),
            MedicationError.START_DATE_REQUIRED
        )
    }

    @Test
    fun validateSchedule_whenStartIsAfterEnd_rejectsRange() {
        assertFailure(
            validator.validateSchedule(
                isRegular = false,
                startDate = 2L,
                endDate = 1L
            ),
            MedicationError.START_DATE_AFTER_END_DATE
        )
    }

    @Test
    fun validateSchedule_allowsEqualDatesAndMissingEndDate() {
        assertTrue(
            validator.validateSchedule(false, startDate = 1L, endDate = 1L) is AppResult.Success<*>
        )
        assertTrue(
            validator.validateSchedule(false, startDate = 1L, endDate = null) is AppResult.Success<*>
        )
    }

    private fun assertFailure(
        result: AppResult<MedicationError, Unit>,
        expected: MedicationError
    ) {
        assertEquals(expected, (result as AppResult.Failure).error)
    }
}
