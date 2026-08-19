package com.example.petvitals.ui.screens.managerecord

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.Record
import com.example.petvitals.domain.models.RecordOverview
import com.example.petvitals.domain.repository.PetRepository
import com.example.petvitals.domain.usecase.GetCurrentUserRecords
import com.example.petvitals.domain.usecase.SaveRecordUseCase
import com.example.petvitals.domain.validator.RecordDataValidator
import com.example.petvitals.ui.navigation.AddEditRecord
import com.example.petvitals.ui.theme.PetVitalsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Date

class ManageRecordScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadError_hidesFormAndRetryLoadsIt() {
        val petRepository = FakePetRepository(
            AppResult.Failure(FirestoreError.Network),
            AppResult.Success(listOf(PET))
        )
        val viewModel = createViewModel(petRepository = petRepository)
        setContent(viewModel, AddEditRecord())

        composeRule.onNodeWithText(string(R.string.network_error)).assertExists()
        composeRule.onNodeWithText(string(R.string.title)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.retry)).performClick()

        composeRule.onNodeWithText(string(R.string.title)).assertExists()
        assertEquals(2, petRepository.calls)
    }

    @Test
    fun saveConflict_keepsDraftAndReloadsLatestExplicitly() {
        val overview = RecordOverview(RECORD, listOf(PET), canManage = true)
        val getRecords = FakeGetCurrentUserRecords(
            AppResult.Success(listOf(overview)),
            AppResult.Success(listOf(overview))
        )
        val viewModel = createViewModel(
            petRepository = FakePetRepository(
                AppResult.Success(listOf(PET)),
                AppResult.Success(listOf(PET))
            ),
            getRecords = getRecords,
            saveRecord = FakeSaveRecordUseCase(AppResult.Failure(FirestoreError.Conflict))
        )
        setContent(viewModel, AddEditRecord(RECORD.id))

        composeRule.onNode(hasText(RECORD.title) and hasSetTextAction())
            .performTextReplacement(DRAFT_TITLE)
        composeRule.onNodeWithText(string(R.string.save)).performScrollTo().performClick()
        composeRule.onNodeWithText(string(R.string.record_changed_error))
            .performScrollTo()
            .assertExists()
        composeRule.onNodeWithText(DRAFT_TITLE).assertExists()
        composeRule.onNodeWithText(string(R.string.reload_latest)).performClick()

        assertEquals(2, getRecords.calls)
        composeRule.onNodeWithText(RECORD.title).assertExists()
        composeRule.onNodeWithText(DRAFT_TITLE).assertDoesNotExist()
    }

    @Test
    fun selectedPet_hasAccessibleRemoveAction() {
        val viewModel = createViewModel(
            petRepository = FakePetRepository(AppResult.Success(listOf(PET)))
        )
        setContent(viewModel, AddEditRecord())
        composeRule.runOnIdle {
            viewModel.onAction(ManageRecordAction.OnPetToggle(PET.id))
        }

        composeRule.onNodeWithContentDescription(string(R.string.remove_pet, PET.name))
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(viewModel.uiState.value.selectedPetIds.isEmpty())
        }
    }

    private fun setContent(viewModel: ManageRecordViewModel, route: AddEditRecord) {
        composeRule.setContent {
            PetVitalsTheme {
                ManageRecordScreen(
                    addEditRecord = route,
                    onPopBackStack = {},
                    viewModel = viewModel
                )
            }
        }
    }

    private fun createViewModel(
        petRepository: PetRepository = FakePetRepository(AppResult.Success(listOf(PET))),
        getRecords: GetCurrentUserRecords = FakeGetCurrentUserRecords(
            AppResult.Success(emptyList())
        ),
        saveRecord: SaveRecordUseCase = FakeSaveRecordUseCase(AppResult.Success(Unit))
    ) = ManageRecordViewModel(
        petRepository = petRepository,
        getCurrentUserRecords = getRecords,
        saveRecordUseCase = saveRecord,
        recordValidator = RecordDataValidator()
    )

    private fun string(resId: Int, vararg args: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId, *args)

    private class FakePetRepository(
        vararg results: AppResult<FirestoreError, List<Pet>>
    ) : PetRepository {
        private val results = ArrayDeque(results.toList())
        var calls = 0

        override suspend fun getCurrentUserPets(): AppResult<FirestoreError, List<Pet>> {
            calls++
            return if (results.size == 1) results.first() else results.removeFirst()
        }

        override suspend fun savePet(pet: Pet) = AppResult.Success(Unit)
        override suspend fun updatePet(pet: Pet) = AppResult.Success(Unit)
        override suspend fun getPetById(petId: String) = AppResult.Success(null)
        override suspend fun deletePet(petId: String) = AppResult.Success(Unit)
        override suspend fun createPetWithOwner(pet: Pet, member: Member) = AppResult.Success(Unit)
    }

    private class FakeGetCurrentUserRecords(
        vararg results: AppResult<FirestoreError, List<RecordOverview>>
    ) : GetCurrentUserRecords {
        private val results = ArrayDeque(results.toList())
        var calls = 0

        override suspend fun invoke(): AppResult<FirestoreError, List<RecordOverview>> {
            calls++
            return if (results.size == 1) results.first() else results.removeFirst()
        }
    }

    private class FakeSaveRecordUseCase(
        private val result: AppResult<FirestoreError, Unit>
    ) : SaveRecordUseCase {
        override suspend fun invoke(
            record: Record,
            previousPetIds: List<String>
        ): AppResult<FirestoreError, Unit> = result
    }

    private companion object {
        val PET = Pet(
            id = "pet-id",
            name = "Milo",
            currentUserPermission = PermissionLevel.OWNER
        )
        val RECORD = Record(
            id = "record-id",
            title = "Annual checkup",
            petIds = listOf(PET.id),
            createdAt = Date(1_000L),
            eventDate = Date(2_000L),
            revision = 2
        )
        const val DRAFT_TITLE = "My unsaved draft"
    }
}
