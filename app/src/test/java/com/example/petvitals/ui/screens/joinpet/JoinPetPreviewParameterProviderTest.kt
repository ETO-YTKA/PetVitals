package com.example.petvitals.ui.screens.joinpet

import org.junit.Assert.assertTrue
import org.junit.Test

class JoinPetPreviewParameterProviderTest {

    @Test
    fun values_coverCoreScreenStates() {
        val states = JoinPetPreviewParameterProvider().values.toList()

        assertTrue(states.any { it.code.isEmpty() })
        assertTrue(states.any { it.code.isNotEmpty() && !it.isCodeComplete })
        assertTrue(states.any {
            it.isCodeComplete && it.errorMessageRes == null && !it.isSubmitting
        })
        assertTrue(states.any { it.errorMessageRes != null && !it.isSubmitting })
        assertTrue(states.any { it.isSubmitting && it.errorMessageRes == null })
    }
}
