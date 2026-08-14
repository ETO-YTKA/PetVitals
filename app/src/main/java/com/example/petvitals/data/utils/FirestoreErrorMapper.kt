package com.example.petvitals.data.utils

import com.example.petvitals.domain.error.FirestoreError
import com.google.firebase.firestore.FirebaseFirestoreException

fun FirebaseFirestoreException.toFirestoreError(): FirestoreError {
    return when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            FirestoreError.PermissionDenied

        FirebaseFirestoreException.Code.UNAUTHENTICATED ->
            FirestoreError.Unauthenticated

        FirebaseFirestoreException.Code.UNAVAILABLE,
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
            FirestoreError.Network

        FirebaseFirestoreException.Code.ABORTED ->
            FirestoreError.Conflict

        else -> FirestoreError.Unknown
    }
}