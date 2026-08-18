package com.example.petvitals.ui.screens.joinpet

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.example.petvitals.R
import com.example.petvitals.ui.theme.PetVitalsTheme
import org.junit.Rule
import org.junit.Test

class JoinPetScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyCode_disablesJoinAndShowsCoreGuidance() {
        setContent(JoinPetUiState())

        composeRule.onNodeWithText(string(R.string.enter_invite_code)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.use_invite_code)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.join_pet)).assertIsNotEnabled()
    }

    @Test
    fun completeCode_enablesJoin() {
        val code = "ABCD1234EFGH5678"
        setContent(JoinPetUiState(code = code))

        composeRule.onNodeWithText(string(R.string.join_pet)).assertIsEnabled()
    }

    @Test
    fun pasteAction_isVisible() {
        setContent(JoinPetUiState())

        composeRule.onNodeWithText(string(R.string.paste)).assertIsDisplayed()
    }

    @Test
    fun errorState_isExplicit() {
        setContent(
            JoinPetUiState(
                code = "ABCD1234EFGH5678",
                errorMessageRes = R.string.invalid_invite_code
            )
        )

        composeRule.onNodeWithText(string(R.string.invalid_invite_code)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.join_pet)).assertIsEnabled()
    }

    @Test
    fun submittingState_disablesActions() {
        setContent(
            JoinPetUiState(
                code = "ABCD1234EFGH5678",
                isSubmitting = true
            )
        )

        composeRule.onNodeWithText(string(R.string.joining_pet)).assertIsNotEnabled()
        composeRule.onNodeWithText(string(R.string.paste)).assertIsNotEnabled()
    }

    private fun setContent(
        uiState: JoinPetUiState
    ) {
        composeRule.setContent {
            PetVitalsTheme {
                JoinPetScreenContent(
                    uiState = uiState,
                    onCodeChange = {},
                    onNavigateBack = {},
                    onAction = {}
                )
            }
        }
    }

    private fun string(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}
