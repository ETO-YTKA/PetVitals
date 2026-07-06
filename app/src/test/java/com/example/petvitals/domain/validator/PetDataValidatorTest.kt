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
    fun validateDobParts_withNoDobParts_returnsSuccess() {
        val result = validator.validateDobParts(
            year = "",
            month = null,
            day = ""
        )

        assertTrue(result is AppResult.Success<*>)
    }

    @Test
    fun validateDobParts_withYearOnly_returnsSuccess() {
        val result = validator.validateDobParts(
            year = "2020",
            month = null,
            day = "",
            currentYear = 2026
        )

        assertTrue(result is AppResult.Success<*>)
    }

    @Test
    fun validateDobParts_withYearAndMonth_returnsSuccess() {
        val result = validator.validateDobParts(
            year = "2020",
            month = 5,
            day = "",
            currentYear = 2026
        )

        assertTrue(result is AppResult.Success<*>)
    }

    @Test
    fun validateDobParts_withCompleteDob_returnsSuccess() {
        val result = validator.validateDobParts(
            year = "2020",
            month = 5,
            day = "12",
            currentYear = 2026
        )

        assertTrue(result is AppResult.Success<*>)
    }

    @Test
    fun validateDobParts_withBlankYearAndMonth_returnsEmptyDobYear() {
        val result = validator.validateDobParts("", month = 5, day = "", currentYear = 2026)

        assertEquals(AppResult.Failure(PetDataError.EMPTY_DOB_YEAR), result)
    }

    @Test
    fun validateDobParts_withDayAndNoYearOrMonth_returnsEmptyDob() {
        val result = validator.validateDobParts("", month = null, day = "12", currentYear = 2026)

        assertEquals(AppResult.Failure(PetDataError.EMPTY_DOB), result)
    }

    @Test
    fun validateDobParts_withDayAndNoMonth_returnsEmptyDob() {
        val result = validator.validateDobParts("2020", month = null, day = "12", currentYear = 2026)

        assertEquals(AppResult.Failure(PetDataError.EMPTY_DOB), result)
    }

    @Test
    fun validateDobParts_withNonNumericYear_returnsInvalidDobYear() {
        val result = validator.validateDobParts("20ab", month = null, day = "", currentYear = 2026)

        assertEquals(AppResult.Failure(PetDataError.INVALID_DOB_YEAR), result)
    }

    @Test
    fun validateDobParts_withFutureYear_returnsDobYearInFuture() {
        val result = validator.validateDobParts("2027", month = null, day = "", currentYear = 2026)

        assertEquals(AppResult.Failure(PetDataError.DOB_YEAR_IN_FUTURE), result)
    }

    @Test
    fun validateDobParts_withInvalidDayForMonth_returnsInvalidDobDay() {
        val result = validator.validateDobParts("2021", month = 2, day = "29", currentYear = 2026)

        assertEquals(AppResult.Failure(PetDataError.INVALID_DOB_DAY), result)
    }
}
