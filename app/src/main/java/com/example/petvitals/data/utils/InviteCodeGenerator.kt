package com.example.petvitals.data.utils

import jakarta.inject.Inject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale

class InviteCodeGenerator @Inject constructor() {

    private val random = SecureRandom()

    fun generate(): String {
        val canonical = buildString(CODE_LENGTH) {
            repeat(CODE_LENGTH) {
                append(ALPHABET[random.nextInt(ALPHABET.length)])
            }
        }

        return canonical.chunked(4).joinToString("-")
    }

    fun normalize(input: String): String =
        input
            .uppercase(Locale.ROOT)
            .filterNot(Char::isWhitespace)
            .replace("-", "")
            .replace('O', '0')
            .replace('I', '1')
            .replace('L', '1')

    fun hash(normalizedCode: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(normalizedCode.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    private companion object {
        const val CODE_LENGTH = 16
        const val ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
    }
}