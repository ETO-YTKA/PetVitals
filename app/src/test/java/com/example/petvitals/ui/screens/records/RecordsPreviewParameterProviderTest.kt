package com.example.petvitals.ui.screens.records

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordsPreviewParameterProviderTest {

    @Test
    fun values_coverAllRecordsScreenStates() {
        val states = RecordsPreviewParameterProvider().values.toList()

        assertEquals(6, states.size)
        assertTrue(states[0].records.isNotEmpty())
        assertTrue(states[1].expandedRecordIds.isNotEmpty())
        assertFalse(states[2].isInitialLoading)
        assertTrue(states[2].records.isEmpty())
        assertTrue(states[3].isInitialLoading)
        assertNotNull(states[4].errorMessageRes)
        assertFalse(states[4].isInitialLoading)
        assertTrue(states[5].searchQuery.isNotBlank())
        assertTrue(states[5].records.isNotEmpty())
    }
}
