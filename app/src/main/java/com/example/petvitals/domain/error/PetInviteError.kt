package com.example.petvitals.domain.error

import com.example.petvitals.domain.AppError

sealed interface PetInviteError : AppError {
    data object InviteUnavailable : PetInviteError
    data object AlreadyMember : PetInviteError
    data object CodeCollision : PetInviteError
    data object InvalidCodeFormat : PetInviteError
    data object Network : PetInviteError
    data object PermissionDenied : PetInviteError
    data object Unauthenticated : PetInviteError
    data object Conflict : PetInviteError
    data object Unknown : PetInviteError
}
