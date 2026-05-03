package com.example.petvitals.domain.error

import com.example.petvitals.domain.AppError

enum class NetworkError: AppError {
    NO_INTERNET,
    REQUEST_TIMEOUT,
    SERVER_ERROR,
    UNKNOWN
}
