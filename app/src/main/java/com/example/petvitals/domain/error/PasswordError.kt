package com.example.petvitals.domain.error

enum class PasswordError: Error {
    EMPTY_FIELD,
    HAS_WHITESPACE,
    TOO_SHORT,
    NO_DIGIT,
    NO_UPPERCASE,
    NO_LOWERCASE
}
