package com.example.petvitals.ui.utils

import androidx.annotation.StringRes
import com.example.petvitals.R
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.error.PetInviteError

@StringRes
fun FirestoreError.toMessageRes(): Int = when (this) {
    FirestoreError.Network -> R.string.network_error
    FirestoreError.PermissionDenied -> R.string.something_went_wrong_error
    FirestoreError.Unauthenticated -> R.string.session_expired_error
    FirestoreError.Conflict -> R.string.unexpected_error
    FirestoreError.Unknown -> R.string.unexpected_error
}

@StringRes
fun PetInviteError.toMessageRes(): Int = when (this) {
    PetInviteError.InviteUnavailable,
    PetInviteError.InvalidCodeFormat -> R.string.invalid_invite_code

    PetInviteError.AlreadyMember -> R.string.already_pet_member_error
    PetInviteError.Network -> R.string.network_error
    PetInviteError.PermissionDenied -> R.string.something_went_wrong_error
    PetInviteError.Unauthenticated -> R.string.session_expired_error
    PetInviteError.CodeCollision,
    PetInviteError.Conflict,
    PetInviteError.Unknown -> R.string.unexpected_error
}