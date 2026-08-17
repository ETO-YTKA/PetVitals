package com.example.petvitals.ui.screens.sharepet

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.example.petvitals.R
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.PetInvite
import com.example.petvitals.ui.theme.PetVitalsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SharePetScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun accessRoster_keepsOwnerImmutable() {
        val actions = mutableListOf<SharePetAction>()
        setContent(state = loadedState(), actions = actions)

        composeRule.onNodeWithText("Morgan").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.permission_level_owner)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(
            string(R.string.remove_access_for, "Morgan")
        ).assertDoesNotExist()
    }

    @Test
    fun memberRemoval_dispatchesOnlyAfterConfirmation() {
        val actions = mutableListOf<SharePetAction>()
        setContent(state = loadedState(), actions = actions)

        composeRule.onNodeWithContentDescription(
            string(R.string.remove_access_for, "Avery")
        ).performClick()

        composeRule.onNodeWithText(
            string(R.string.share_pet_remove_access_for, "Avery")
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            string(
                R.string.will_no_longer_be_able_to_view_this_pet_or_make_changes_to_it,
                "Avery"
            )
        ).assertIsDisplayed()
        assertTrue(actions.isEmpty())

        composeRule.onNodeWithText(string(R.string.remove_access)).performClick()

        assertEquals(listOf(SharePetAction.OnRemoveMember("editor-id")), actions)
        composeRule.onNodeWithText(
            string(R.string.share_pet_remove_access_for, "Avery")
        ).assertDoesNotExist()
    }

    @Test
    fun memberRemoval_keepAccessDismissesWithoutAction() {
        val actions = mutableListOf<SharePetAction>()
        setContent(state = loadedState(), actions = actions)

        composeRule.onNodeWithContentDescription(
            string(R.string.remove_access_for, "Avery")
        ).performClick()
        composeRule.onNodeWithText(string(R.string.keep_access)).performClick()

        composeRule.onNodeWithText(
            string(R.string.share_pet_remove_access_for, "Avery")
        ).assertDoesNotExist()
        assertTrue(actions.isEmpty())
    }

    @Test
    fun inviteComposer_dispatchesSelectedPermissionAndCreate() {
        val actions = mutableListOf<SharePetAction>()
        setContent(state = loadedState(), actions = actions)

        composeRule.onNode(
            hasText(string(R.string.permission_level_editor)) and hasClickAction()
        )
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText(string(R.string.generate_code))
            .performScrollTo()
            .performClick()

        assertEquals(
            listOf(
                SharePetAction.OnSelectInvitePermission(PermissionLevel.EDITOR),
                SharePetAction.OnCreateInviteCode
            ),
            actions
        )
    }

    @Test
    fun generatedCode_copyAndShareDispatchCodeActions() {
        val actions = mutableListOf<SharePetAction>()
        setContent(
            state = loadedState().copy(latestGeneratedCode = RAW_CODE),
            actions = actions
        )

        composeRule.onNodeWithText(RAW_CODE).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.copy_code)).performScrollTo().performClick()
        composeRule.onNodeWithText(string(R.string.share_code)).performScrollTo().performClick()

        assertEquals(2, actions.size)
        assertTrue(
            actions[0] is SharePetAction.OnCopyInviteCode &&
                (actions[0] as SharePetAction.OnCopyInviteCode).code == RAW_CODE
        )
        assertTrue(
            actions[1] is SharePetAction.OnShareInviteCode &&
                (actions[1] as SharePetAction.OnShareInviteCode).code == RAW_CODE
        )
    }

    @Test
    fun activeInvite_dispatchesRevocationForItsHash() {
        val actions = mutableListOf<SharePetAction>()
        setContent(
            state = loadedState().copy(activeInvites = listOf(EDITOR_INVITE)),
            actions = actions
        )

        composeRule.onNodeWithText(string(R.string.active_invites))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription(string(R.string.revoke_invite))
            .performScrollTo()
            .performClick()

        assertEquals(
            listOf(SharePetAction.OnRevokeInviteCode(EDITOR_INVITE.codeHash)),
            actions
        )
    }

    @Test
    fun inviteErrors_areVisibleInTheirSections() {
        setContent(
            state = loadedState().copy(
                createInviteErrorMessageRes = R.string.network_error,
                invitesErrorMessageRes = R.string.something_went_wrong_error
            )
        )

        composeRule.onNodeWithText(string(R.string.network_error))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.something_went_wrong_error))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun loadingState_exposesIndeterminateProgressSemantics() {
        setContent(state = SharePetUiState(isLoading = true))

        composeRule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)
        ).assertExists()
    }

    private fun setContent(
        state: SharePetUiState,
        actions: MutableList<SharePetAction> = mutableListOf()
    ) {
        composeRule.setContent {
            PetVitalsTheme {
                SharePetScreenContent(
                    uiState = state,
                    snackbarHostState = SnackbarHostState(),
                    onAction = actions::add,
                    onPopBackStack = {}
                )
            }
        }
    }

    private fun loadedState() = SharePetUiState(
        petId = PET_ID,
        petMembers = listOf(
            Member("owner-id", "Morgan", PermissionLevel.OWNER),
            Member("editor-id", "Avery", PermissionLevel.EDITOR)
        )
    )

    private fun string(@StringRes id: Int, vararg args: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)

    private companion object {
        const val PET_ID = "pet-id"
        const val RAW_CODE = "ABCD-EFGH-JKLM-NPQR"
        val EDITOR_INVITE = PetInvite("editor-hash", PET_ID, PermissionLevel.EDITOR)
    }
}
