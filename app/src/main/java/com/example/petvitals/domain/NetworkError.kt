package com.example.petvitals.domain

enum class NetworkError: Error {
    NO_INTERNET,
    REQUEST_TIMEOUT,
    SERVER_ERROR,
    UNKNOWN
}