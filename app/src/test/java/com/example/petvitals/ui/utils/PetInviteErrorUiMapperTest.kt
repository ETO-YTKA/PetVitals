package com.example.petvitals.ui.utils

import com.example.petvitals.R
import com.example.petvitals.domain.error.PetInviteError
import org.junit.Assert.assertEquals
import org.junit.Test

class PetInviteErrorUiMapperTest {

    @Test
    fun inviteErrors_mapToUserFacingMessages() {
        val cases = mapOf(
            PetInviteError.InviteUnavailable to R.string.invalid_invite_code,
            PetInviteError.InvalidCodeFormat to R.string.invalid_invite_code,
            PetInviteError.AlreadyMember to R.string.already_pet_member_error,
            PetInviteError.CodeCollision to R.string.unexpected_error,
            PetInviteError.Network to R.string.network_error,
            PetInviteError.PermissionDenied to R.string.something_went_wrong_error,
            PetInviteError.Unauthenticated to R.string.session_expired_error,
            PetInviteError.Conflict to R.string.unexpected_error,
            PetInviteError.Unknown to R.string.unexpected_error
        )

        cases.forEach { (error, expected) ->
            assertEquals(expected, error.toMessageRes())
        }
    }
}
