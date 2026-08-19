package com.example.petvitals.domain

import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.error.PetInviteError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AppResultTest {

    @Test
    fun mapError_transformsFailure() {
        val result: AppResult<FirestoreError, String> =
            AppResult.Failure(FirestoreError.Network)

        val mapped = result.mapError { PetInviteError.Network }

        assertSame(PetInviteError.Network, (mapped as AppResult.Failure).error)
    }

    @Test
    fun mapError_preservesSuccessData() {
        val result: AppResult<FirestoreError, String> = AppResult.Success("pet-id")

        val mapped = result.mapError { PetInviteError.Unknown }

        assertEquals("pet-id", (mapped as AppResult.Success).data)
    }
}
