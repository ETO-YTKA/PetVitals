package com.example.petvitals.ui.screens.records

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.petvitals.R
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.Record
import com.example.petvitals.domain.models.RecordOverview
import com.example.petvitals.domain.models.RecordType
import com.example.petvitals.ui.theme.PetVitalsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class RecordsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun addAction_usesDirectNavigationCallback() {
        var navigated = false
        setRecordsContent(
            uiState = RecordsUiState(),
            onNavigateToAddRecord = { navigated = true }
        )

        composeRule.onNodeWithText(
            string(R.string.new_record),
            useUnmergedTree = true
        ).performClick()

        composeRule.runOnIdle { assertTrue(navigated) }
    }

    @Test
    fun addAction_hasAccessibleName() {
        setRecordsContent(uiState = previewState())

        composeRule.onNode(
            hasContentDescription(string(R.string.new_record)) and hasClickAction()
        ).assertExists()
    }

    @Test
    fun petFilterChip_dispatchesFilterAction() {
        val actions = mutableListOf<RecordsAction>()
        setRecordsContent(
            uiState = previewState(),
            onAction = actions::add
        )

        composeRule.onNodeWithTag("pet-filter-${testPet.id}").performClick()

        composeRule.runOnIdle {
            assertEquals(RecordsAction.OnPetFilterToggle(testPet.id), actions.single())
        }
    }

    @Test
    fun typeFilterChip_dispatchesFilterAction() {
        val actions = mutableListOf<RecordsAction>()
        setRecordsContent(
            uiState = previewState(),
            onAction = actions::add
        )

        composeRule.onNodeWithTag("type-filter-${RecordType.VACCINATION.name}").performClick()

        composeRule.runOnIdle {
            assertEquals(
                RecordsAction.OnTypeFilterToggle(RecordType.VACCINATION),
                actions.single()
            )
        }
    }

    @Test
    fun deleteMenu_dispatchesRecordsAction() {
        val actions = mutableListOf<RecordsAction>()
        setRecordsContent(
            uiState = previewState(),
            onAction = actions::add
        )

        composeRule.onNodeWithContentDescription(string(R.string.more_options)).performClick()
        composeRule.onNodeWithText(string(R.string.delete)).performClick()

        composeRule.onNodeWithText(string(R.string.delete_record_title)).assertExists()
        composeRule.onNodeWithText(string(R.string.delete)).performClick()

        composeRule.runOnIdle {
            assertEquals(RecordsAction.OnDeleteRecordClick(testRecord.id), actions.single())
        }
    }

    @Test
    fun noResults_clearFiltersDispatchesClearAction() {
        val actions = mutableListOf<RecordsAction>()
        setRecordsContent(
            uiState = previewState().copy(searchQuery = "dental"),
            onAction = actions::add
        )

        composeRule.onNodeWithText(string(R.string.clear_filters)).performClick()

        composeRule.runOnIdle {
            assertEquals(RecordsAction.OnClearFilters, actions.single())
        }
    }

    @Test
    fun searchInput_dispatchesQueryAction() {
        val actions = mutableListOf<RecordsAction>()
        setRecordsContent(
            uiState = previewState(),
            onAction = actions::add
        )

        composeRule.onNode(hasText(string(R.string.records_search_label)) and hasClickAction())
            .performTextInput("vet")

        composeRule.runOnIdle {
            assertTrue(RecordsAction.OnSearchQueryChange("vet") in actions)
        }
    }

    @Test
    fun recordTap_dispatchesExpansionAction() {
        val actions = mutableListOf<RecordsAction>()
        setRecordsContent(
            uiState = previewState(),
            onAction = actions::add
        )

        composeRule.onNode(hasText(testRecord.title) and hasClickAction()).performClick()

        composeRule.runOnIdle {
            assertEquals(
                RecordsAction.OnRecordExpansionToggle(testRecord.id),
                actions.single()
            )
        }
    }

    @Test
    fun recordRow_exposesFloatingTypeMedallion() {
        setRecordsContent(uiState = previewState())

        composeRule.onNodeWithTag("record-type-gutter-${testRecord.id}")
            .assertWidthIsEqualTo(44.dp)
        composeRule.onNodeWithTag("record-type-medallion-${testRecord.id}")
            .assertWidthIsEqualTo(40.dp)
            .assertHeightIsEqualTo(40.dp)
    }

    @Test
    fun profileAction_usesDirectNavigationCallback() {
        var navigated = false
        setRecordsContent(
            uiState = previewState(),
            onNavigateToProfile = { navigated = true }
        )

        composeRule.onNodeWithContentDescription(string(R.string.profile)).performClick()

        composeRule.runOnIdle { assertTrue(navigated) }
    }

    @Test
    fun petChip_usesDirectNavigationCallback() {
        var navigatedPetId: String? = null
        setRecordsContent(
            uiState = previewState(),
            onNavigateToPetProfile = { navigatedPetId = it }
        )

        composeRule.onNodeWithTag("record-pet-${testPet.id}").performClick()

        composeRule.runOnIdle { assertEquals(testPet.id, navigatedPetId) }
    }

    @Test
    fun editMenu_usesDirectNavigationCallback() {
        var navigatedRecordId: String? = null
        setRecordsContent(
            uiState = previewState(),
            onNavigateToEditRecord = { navigatedRecordId = it }
        )

        composeRule.onNodeWithContentDescription(string(R.string.more_options)).performClick()
        composeRule.onNodeWithText(string(R.string.edit)).performClick()

        composeRule.runOnIdle { assertEquals(testRecord.id, navigatedRecordId) }
    }

    private fun setRecordsContent(
        uiState: RecordsUiState,
        onAction: (RecordsAction) -> Unit = {},
        onNavigateToAddRecord: () -> Unit = {},
        onNavigateToEditRecord: (String) -> Unit = {},
        onNavigateToProfile: () -> Unit = {},
        onNavigateToPetProfile: (String) -> Unit = {}
    ) {
        composeRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }

            PetVitalsTheme {
                Surface {
                    RecordsScreenContent(
                        uiState = uiState,
                        snackbarHostState = snackbarHostState,
                        onAction = onAction,
                        onNavigateToAddRecord = onNavigateToAddRecord,
                        onNavigateToEditRecord = onNavigateToEditRecord,
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToPetProfile = onNavigateToPetProfile
                    )
                }
            }
        }
    }

    private fun previewState(): RecordsUiState {
        val overview = RecordOverview(
            record = testRecord,
            pets = listOf(testPet),
            canManage = true
        )
        return RecordsUiState(
            isInitialLoading = false,
            records = listOf(overview)
        )
    }

    private fun string(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

    private companion object {
        val testPet = Pet(id = "pet-1", name = "Mochi")
        val testRecord = Record(
            id = "record-1",
            title = "Annual vaccination",
            type = RecordType.VACCINATION,
            eventDate = Date(1_775_565_600_000L),
            petIds = listOf(testPet.id)
        )
    }
}
