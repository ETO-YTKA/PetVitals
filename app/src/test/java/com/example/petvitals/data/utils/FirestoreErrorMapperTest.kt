package com.example.petvitals.data.utils

import com.example.petvitals.domain.error.FirestoreError
import org.junit.Assert.assertEquals
import org.junit.Test

class FirestoreErrorMapperTest {

    @Test
    fun aborted_mapsToConflict() {
        assertEquals(
            FirestoreError.Conflict,
            firestoreErrorForCode("ABORTED")
        )
    }
}
