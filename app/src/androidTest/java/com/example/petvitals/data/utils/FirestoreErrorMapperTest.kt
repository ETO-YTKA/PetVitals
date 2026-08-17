package com.example.petvitals.data.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.petvitals.domain.error.FirestoreError
import com.google.firebase.firestore.FirebaseFirestoreException
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirestoreErrorMapperTest {

    @Test
    fun aborted_mapsToConflict() {
        val exception = FirebaseFirestoreException(
            "Transaction conflict",
            FirebaseFirestoreException.Code.ABORTED
        )

        assertEquals(FirestoreError.Conflict, exception.toFirestoreError())
    }
}
