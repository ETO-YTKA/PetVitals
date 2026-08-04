package com.example.petvitals.ui.components

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.example.petvitals.ui.theme.PetVitalsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DatePickerFieldTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun field_semanticClickInvokesDatePickerAction() {
        var clickCount = 0
        composeRule.setContent {
            PetVitalsTheme {
                DatePickerField(
                    value = "Tap to select date",
                    onClick = { clickCount++ },
                    label = "Start date"
                )
            }
        }

        composeRule
            .onNode(hasText("Start date") and hasClickAction())
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, clickCount)
        }
    }

    @Test
    fun clearClick_clearsValueWithoutOpeningDatePicker() {
        var clickCount = 0
        var clearCount = 0
        composeRule.setContent {
            PetVitalsTheme {
                DatePickerField(
                    value = "August 4, 2026",
                    onClick = { clickCount++ },
                    label = "End date (optional)",
                    onClear = { clearCount++ },
                    clearContentDescription = "Clear end date"
                )
            }
        }

        composeRule
            .onNodeWithContentDescription("Clear end date")
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, clearCount)
            assertEquals(0, clickCount)
        }
    }
}
