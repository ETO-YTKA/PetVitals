package com.example.petvitals.domain.error

enum class DisplayNameError: Error {
    EMPTY_FIELD,
    TOO_LONG,
    INVALID_CHARACTERS
}
