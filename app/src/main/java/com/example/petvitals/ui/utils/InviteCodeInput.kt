package com.example.petvitals.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.Locale

internal const val INVITE_CODE_LENGTH = 16
private const val INVITE_CODE_ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"

internal fun normalizeInviteCodeInput(input: String): String =
    input
        .uppercase(Locale.ROOT)
        .asSequence()
        .map { character ->
            when (character) {
                'O' -> '0'
                'I', 'L' -> '1'
                else -> character
            }
        }
        .filter { it in INVITE_CODE_ALPHABET }
        .take(INVITE_CODE_LENGTH)
        .joinToString("")

internal fun formatInviteCode(code: String): String =
    code.chunked(4).joinToString("-")

internal object InviteCodeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val canonical = text.text.take(INVITE_CODE_LENGTH)
        val transformed = AnnotatedString(formatInviteCode(canonical))

        return TransformedText(
            text = transformed,
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    val clamped = offset.coerceIn(0, canonical.length)
                    val separators = (clamped / 4).coerceAtMost(3)
                    return (clamped + separators).coerceAtMost(transformed.length)
                }

                override fun transformedToOriginal(offset: Int): Int {
                    val clamped = offset.coerceIn(0, transformed.length)
                    val separators = when {
                        clamped <= 4 -> 0
                        clamped <= 9 -> 1
                        clamped <= 14 -> 2
                        else -> 3
                    }
                    return (clamped - separators).coerceIn(0, canonical.length)
                }
            }
        )
    }
}
