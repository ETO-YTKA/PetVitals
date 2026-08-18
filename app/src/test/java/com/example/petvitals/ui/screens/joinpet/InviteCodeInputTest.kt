package com.example.petvitals.ui.screens.joinpet

import androidx.compose.ui.text.AnnotatedString
import com.example.petvitals.ui.utils.InviteCodeVisualTransformation
import com.example.petvitals.ui.utils.formatInviteCode
import com.example.petvitals.ui.utils.normalizeInviteCodeInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InviteCodeInputTest {

    @Test
    fun normalize_removesSeparatorsAndCanonicalizesAliases() {
        assertEquals("ABCD0112", normalizeInviteCodeInput(" abcd-OIl2 "))
    }

    @Test
    fun normalize_filtersUnsupportedCharactersAndCapsLength() {
        assertEquals(
            "ABCDEFGHJKMNPQRS",
            normalizeInviteCodeInput("ABCD-!EFGH_JKMN-PQRS-EXTRA")
        )
    }

    @Test
    fun format_groupsCanonicalCodeWithoutChangingItsValue() {
        assertEquals("ABCD-1234-EFGH-5678", formatInviteCode("ABCD1234EFGH5678"))
    }

    @Test
    fun visualTransformation_mapsCursorAcrossEverySeparator() {
        val transformed = InviteCodeVisualTransformation.filter(
            AnnotatedString("ABCD1234EFGH5678")
        )

        assertEquals("ABCD-1234-EFGH-5678", transformed.text.text)
        assertEquals(0, transformed.offsetMapping.originalToTransformed(0))
        assertEquals(5, transformed.offsetMapping.originalToTransformed(4))
        assertEquals(10, transformed.offsetMapping.originalToTransformed(8))
        assertEquals(15, transformed.offsetMapping.originalToTransformed(12))
        assertEquals(19, transformed.offsetMapping.originalToTransformed(16))
        assertEquals(4, transformed.offsetMapping.transformedToOriginal(4))
        assertEquals(4, transformed.offsetMapping.transformedToOriginal(5))
        assertEquals(8, transformed.offsetMapping.transformedToOriginal(9))
        assertEquals(8, transformed.offsetMapping.transformedToOriginal(10))
        assertEquals(12, transformed.offsetMapping.transformedToOriginal(14))
        assertEquals(12, transformed.offsetMapping.transformedToOriginal(15))
        assertEquals(16, transformed.offsetMapping.transformedToOriginal(19))
    }

    @Test
    fun state_isCompleteOnlyAtSixteenCharacters() {
        assertFalse(JoinPetUiState(code = "ABCD").isCodeComplete)
        assertTrue(JoinPetUiState(code = "ABCD1234EFGH5678").isCodeComplete)
    }
}
