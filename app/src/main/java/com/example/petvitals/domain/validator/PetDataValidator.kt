package com.example.petvitals.domain.validator

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.PetDataError
import com.example.petvitals.domain.models.PetSpecies
import jakarta.inject.Inject
import java.time.DateTimeException
import java.time.LocalDate

class PetDataValidator @Inject constructor() {

    fun validateName(name: String): AppResult<PetDataError, Unit> {
        val trimmedName = name.trim()

        if (trimmedName.isEmpty()) {
            return AppResult.Failure(PetDataError.EMPTY_NAME)
        }

        if (trimmedName.length > MAX_NAME_LENGTH) {
            return AppResult.Failure(PetDataError.NAME_TOO_LONG)
        }

        return AppResult.Success(Unit)
    }

    fun validateBreed(breed: String): AppResult<PetDataError, Unit> {
        return if (breed.trim().length > MAX_BREED_LENGTH) {
            AppResult.Failure(PetDataError.BREED_TOO_LONG)
        } else {
            AppResult.Success(Unit)
        }
    }

    fun validateDobParts(
        year: String,
        month: Int?,
        day: String,
        currentYear: Int = LocalDate.now().year
    ): AppResult<PetDataError, Unit> {
        val trimmedYear = year.trim()
        val trimmedDay = day.trim()
        val hasNoDob = trimmedYear.isEmpty() && month == null && trimmedDay.isEmpty()

        if (hasNoDob) {
            return AppResult.Success(Unit)
        }

        if (trimmedDay.isNotEmpty() && (trimmedYear.isEmpty() || month == null)) {
            return AppResult.Failure(PetDataError.EMPTY_DOB)
        }

        if (trimmedYear.isEmpty()) {
            return AppResult.Failure(PetDataError.EMPTY_DOB_YEAR)
        }

        val yearInt = trimmedYear.toIntOrNull()
            ?: return AppResult.Failure(PetDataError.INVALID_DOB_YEAR)

        if (yearInt > currentYear) {
            return AppResult.Failure(PetDataError.DOB_YEAR_IN_FUTURE)
        }

        if (trimmedDay.isEmpty()) {
            return AppResult.Success(Unit)
        }

        val dayInt = trimmedDay.toIntOrNull()
            ?: return AppResult.Failure(PetDataError.INVALID_DOB_DAY)

        try {
            LocalDate.of(yearInt, month ?: return AppResult.Failure(PetDataError.EMPTY_DOB), dayInt)
        } catch (_: DateTimeException) {
            return AppResult.Failure(PetDataError.INVALID_DOB_DAY)
        }

        return AppResult.Success(Unit)
    }

    fun validateSpecies(species: PetSpecies?): AppResult<PetDataError, Unit> {
        return if (species == null) {
            AppResult.Failure(PetDataError.EMPTY_SPECIES)
        } else {
            AppResult.Success(Unit)
        }
    }

    companion object {
        const val MAX_NAME_LENGTH = 50
        const val MAX_BREED_LENGTH = 100
    }
}
