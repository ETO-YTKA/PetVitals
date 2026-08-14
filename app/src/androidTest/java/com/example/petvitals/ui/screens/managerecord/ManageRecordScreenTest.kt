package com.example.petvitals.ui.screens.managerecord

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.petvitals.R
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.ui.theme.PetVitalsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManageRecordScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadError_hidesFormAndDispatchesRetry() {
        val actions = mutableListOf<ManageRecordAction>()
        setContent(
            ManageRecordUiState(loadErrorMessageRes = R.string.network_error),
            actions::add
        )

        composeRule.onNodeWithText(string(R.string.network_error)).assertExists()
        composeRule.onNodeWithText(string(R.string.title)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.retry)).performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(ManageRecordAction.OnRetryLoad), actions)
        }
    }

    @Test
    fun conflict_keepsFormAndDispatchesReloadLatest() {
        val actions = mutableListOf<ManageRecordAction>()
        setContent(
            ManageRecordUiState(
                title = "My draft",
                eventDate = 1_750_000_000_000L,
                createdAt = 1_750_000_000_000L,
                availablePets = listOf(PET),
                selectedPetIds = setOf(PET.id),
                hasConflict = true
            ),
            actions::add
        )

        composeRule.onNodeWithText("My draft").assertExists()
        composeRule.onNodeWithText(string(R.string.record_changed_error)).assertExists()
        composeRule.onNodeWithText(string(R.string.reload_latest)).performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(ManageRecordAction.OnReloadLatest), actions)
        }
    }

    @Test
    fun selectedPet_hasAccessibleRemoveAction() {
        val actions = mutableListOf<ManageRecordAction>()
        setContent(
            ManageRecordUiState(
                eventDate = 1_750_000_000_000L,
                createdAt = 1_750_000_000_000L,
                availablePets = listOf(PET),
                selectedPetIds = setOf(PET.id)
            ),
            actions::add
        )

        composeRule.onNodeWithContentDescription(string(R.string.remove_pet, PET.name))
            .performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(ManageRecordAction.OnPetToggle(PET.id)), actions)
        }
    }

    private fun setContent(
        state: ManageRecordUiState,
        onAction: (ManageRecordAction) -> Unit
    ) {
        composeRule.setContent {
            PetVitalsTheme {
                ManageRecordScreenContent(
                    uiState = state,
                    isEditing = true,
                    snackbarHostState = remember { SnackbarHostState() },
                    onAction = onAction,
                    onPopBackStack = {}
                )
            }
        }
    }

    private fun string(resId: Int, vararg args: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId, *args)

    private companion object {
        val PET = Pet(id = "pet-id", name = "Milo")
    }
}
