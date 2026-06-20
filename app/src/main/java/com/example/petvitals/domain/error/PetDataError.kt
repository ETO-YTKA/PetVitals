package com.example.petvitals.domain.error

import com.example.petvitals.domain.AppError

enum class PetDataError : AppError {
    EMPTY_NAME,
    NAME_TOO_LONG,
    BREED_TOO_LONG,
    EMPTY_DOB,
    EMPTY_DOB_YEAR,
    INVALID_DOB_YEAR,
    DOB_YEAR_IN_FUTURE,
    EMPTY_SPECIES
}
