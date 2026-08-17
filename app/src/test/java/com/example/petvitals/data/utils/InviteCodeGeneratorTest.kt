package com.example.petvitals.data.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InviteCodeGeneratorTest {

    private val generator = InviteCodeGenerator()

    @Test
    fun generate_returnsSixteenCrockfordCharactersGroupedForReading() {
        val code = generator.generate()

        assertTrue(CODE_PATTERN.matches(code))
        assertEquals(16, generator.normalize(code).length)
    }

    @Test
    fun normalize_removesFormattingAndCanonicalizesAmbiguousCharacters() {
        assertEquals(
            "011ABCDEFGHJKMN",
            generator.normalize(" oIl-ABcd efgh-jkmn ")
        )
    }

    @Test
    fun hash_usesStableSha256Hex() {
        val hash = generator.hash("hello")

        assertEquals(
            "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
            hash
        )
        assertNotEquals(hash, generator.hash("HELLO"))
    }

    private companion object {
        val CODE_PATTERN = Regex("^[0-9A-HJKMNP-TV-Z]{4}(-[0-9A-HJKMNP-TV-Z]{4}){3}$")
    }
}
