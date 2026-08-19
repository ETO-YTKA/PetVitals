package com.example.petvitals.data.utils

import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.error.PetInviteError
import org.junit.Assert.assertSame
import org.junit.Test

class PetInviteErrorMapperTest {

    @Test
    fun firestoreErrors_mapToEquivalentInviteErrors() {
        val cases = mapOf(
            FirestoreError.Network to PetInviteError.Network,
            FirestoreError.PermissionDenied to PetInviteError.PermissionDenied,
            FirestoreError.Unauthenticated to PetInviteError.Unauthenticated,
            FirestoreError.Conflict to PetInviteError.Conflict,
            FirestoreError.Unknown to PetInviteError.Unknown
        )

        cases.forEach { (source, expected) ->
            assertSame(expected, source.toPetInviteError())
        }
    }
}
