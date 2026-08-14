package com.example.petvitals.domain.validator

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.RecordValidationError
import javax.inject.Inject

class RecordDataValidator @Inject constructor() {

    fun validateTitle(title: String): AppResult<RecordValidationError, Unit> = when {
        title.length > MAX_TITLE_LENGTH -> AppResult.Failure(
            RecordValidationError.TITLE_TOO_LONG
        )
        else -> AppResult.Success(Unit)
    }

    fun validateDescription(description: String): AppResult<RecordValidationError, Unit> = when {
        description.length > MAX_DESCRIPTION_LENGTH -> AppResult.Failure(
            RecordValidationError.DESCRIPTION_TOO_LONG
        )
        else -> AppResult.Success(Unit)
    }

    fun validatePetIds(petIds: Collection<String>): AppResult<RecordValidationError, Unit> = when {
        petIds.any(String::isNotBlank) -> AppResult.Success(Unit)
        else -> AppResult.Failure(RecordValidationError.PET_REQUIRED)
    }

    private companion object {
        const val MAX_TITLE_LENGTH = 50
        const val MAX_DESCRIPTION_LENGTH = 500
    }
}