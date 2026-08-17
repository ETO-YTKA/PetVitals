package com.example.petvitals.ui.screens.sharepet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SharePetPreviewParameterProviderTest {

    @Test
    fun values_coverSixDistinctScreenStates() {
        val states = SharePetPreviewParameterProvider().values.toList()

        assertEquals(6, states.size)
        assertTrue(states[0].petMembers.size >= 3)
        assertEquals(1, states[1].petMembers.size)
        assertNotNull(states[2].latestGeneratedCode)
        assertTrue(states[3].activeInvites.size >= 2)
        assertTrue(states[4].isLoading)
        assertFalse(states[5].isLoading)
        assertNotNull(states[5].permissionErrorMessageRes)
    }
}
