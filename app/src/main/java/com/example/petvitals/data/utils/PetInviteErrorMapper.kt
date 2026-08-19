package com.example.petvitals.data.utils

import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.error.PetInviteError

fun FirestoreError.toPetInviteError(): PetInviteError = when (this) {
    FirestoreError.Network -> PetInviteError.Network
    FirestoreError.PermissionDenied -> PetInviteError.PermissionDenied
    FirestoreError.Unauthenticated -> PetInviteError.Unauthenticated
    FirestoreError.Conflict -> PetInviteError.Conflict
    FirestoreError.Unknown -> PetInviteError.Unknown
}
