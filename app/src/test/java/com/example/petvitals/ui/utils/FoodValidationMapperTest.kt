package com.example.petvitals.ui.utils

import com.example.petvitals.R
import com.example.petvitals.domain.validator.FoodDataValidator
import org.junit.Assert.assertEquals
import org.junit.Test

class FoodValidationMapperTest {

    @Test
    fun frequencyTooLong_usesFrequencyMessage() {
        val result = FoodDataValidator()
            .validateFrequency("x".repeat(51))
            .toMessageResOrNull()

        assertEquals(R.string.food_frequency_too_long_error, result)
    }
}
