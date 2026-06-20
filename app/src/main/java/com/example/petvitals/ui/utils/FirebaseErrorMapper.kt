package com.example.petvitals.ui.utils

import androidx.annotation.StringRes
import com.example.petvitals.R
import com.example.petvitals.domain.error.FirestoreError

@StringRes
fun FirestoreError.toMessageRes(): Int = when (this) {
    FirestoreError.Network -> R.string.network_error
    FirestoreError.PermissionDenied -> R.string.something_went_wrong_error
    FirestoreError.Unauthenticated -> R.string.session_expired_error
    FirestoreError.Unknown -> R.string.unexpected_error
}