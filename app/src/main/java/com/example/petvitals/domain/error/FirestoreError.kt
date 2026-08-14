package com.example.petvitals.domain.error

import com.example.petvitals.domain.AppError

sealed interface FirestoreError: AppError {
    data object Network : FirestoreError
    data object PermissionDenied : FirestoreError
    data object Unauthenticated : FirestoreError
    data object Conflict : FirestoreError
    data object Unknown : FirestoreError
}
