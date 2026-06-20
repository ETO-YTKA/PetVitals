package com.example.petvitals.domain.validator

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.PetDataError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PetDataValidatorTest {

    private val validator = PetDataValidator()

    @Test
    fun validateName_withBlankName_returnsEmptyName() {
        val result = validator.validateName("   ")

        assertEquals(AppResult.Failure(PetDataError.EMPTY_NAME), result)
    }

    @Test
    fun validateName_withNameLongerThanLimit_returnsNameTooLong() {
        val result = validator.validateName("a".repeat(51))

        assertEquals(AppResult.Failure(PetDataError.NAME_TOO_LONG), result)
    }

    @Test
    fun validateName_withValidName_returnsSuccess() {
        val result = validator.validateName("Mittens")

        assertTrue(result is AppResult.Success<*>)
    }

    @Test
    fun validateBreed_withBreedLongerThanLimit_returnsBreedTooLong() {
        val result = validator.validateBreed("a".repeat(101))

        assertEquals(AppResult.Failure(PetDataError.BREED_TOO_LONG), result)
    }

    @Test
    fun validateBreed_withBlankBreed_returnsSuccess() {
        val result = validator.validateBreed("")

        assertTrue(result is AppResult.Success<*>)
    }

    @Test
    fun validateExactDob_withNullDob_returnsEmptyDob() {
        val result = validator.validateExactDob(null)

        assertEquals(AppResult.Failure(PetDataError.EMPTY_DOB), result)
    }

    @Test
    fun validateApproxDobYear_withBlankYear_returnsEmptyDobYear() {
        val result = validator.validateApproxDobYear("", currentYear = 2026)

        assertEquals(AppResult.Failure(PetDataError.EMPTY_DOB_YEAR), result)
    }

    @Test
    fun validateApproxDobYear_withNonNumericYear_returnsInvalidDobYear() {
        val result = validator.validateApproxDobYear("20ab", currentYear = 2026)

        assertEquals(AppResult.Failure(PetDataError.INVALID_DOB_YEAR), result)
    }

    @Test
    fun validateApproxDobYear_withFutureYear_returnsDobYearInFuture() {
        val result = validator.validateApproxDobYear("2027", currentYear = 2026)

        assertEquals(AppResult.Failure(PetDataError.DOB_YEAR_IN_FUTURE), result)
    }

    @Test
    fun validateApproxDobYear_withValidYear_returnsSuccess() {
        val result = validator.validateApproxDobYear("2020", currentYear = 2026)

        assertTrue(result is AppResult.Success<*>)
    }
}
