package com.example.petvitals.domain.validator

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.RecordValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordDataValidatorTest {

    private val validator = RecordDataValidator()

    @Test
    fun validateTitle_allowsBlankAndFiftyCharacters() {
        assertTrue(validator.validateTitle("") is AppResult.Success<*>)
        assertTrue(validator.validateTitle("x".repeat(50)) is AppResult.Success<*>)
    }

    @Test
    fun validateTitle_rejectsMoreThanFiftyCharacters() {
        assertFailure(
            validator.validateTitle("x".repeat(51)),
            RecordValidationError.TITLE_TOO_LONG
        )
    }

    @Test
    fun validateDescription_allowsFiveHundredCharacters() {
        assertTrue(validator.validateDescription("x".repeat(500)) is AppResult.Success<*>)
    }

    @Test
    fun validateDescription_rejectsMoreThanFiveHundredCharacters() {
        assertFailure(
            validator.validateDescription("x".repeat(501)),
            RecordValidationError.DESCRIPTION_TOO_LONG
        )
    }

    @Test
    fun validatePetIds_requiresAtLeastOneNonBlankPetId() {
        assertFailure(validator.validatePetIds(emptySet()), RecordValidationError.PET_REQUIRED)
        assertFailure(validator.validatePetIds(setOf("")), RecordValidationError.PET_REQUIRED)
        assertTrue(validator.validatePetIds(setOf("pet-id")) is AppResult.Success<*>)
    }

    private fun assertFailure(
        result: AppResult<RecordValidationError, Unit>,
        expected: RecordValidationError
    ) {
        assertEquals(expected, (result as AppResult.Failure).error)
    }
}
