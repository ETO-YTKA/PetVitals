package com.example.petvitals.ui.utils

import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.RecordValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecordValidationMapperTest {

    @Test
    fun mapper_returnsExpectedResources() {
        val expected = mapOf(
            RecordValidationError.TITLE_TOO_LONG to R.string.title_too_long,
            RecordValidationError.DESCRIPTION_TOO_LONG to
                R.string.description_cannot_be_longer_than_error,
            RecordValidationError.PET_REQUIRED to R.string.select_at_least_one_pet_error
        )

        expected.forEach { (error, messageRes) ->
            assertEquals(
                messageRes,
                AppResult.Failure(error).toRecordMessageResOrNull()
            )
        }
        assertNull(AppResult.Success(Unit).toRecordMessageResOrNull())
    }
}
