package com.example.petvitals.ui.screens.managerecord

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
import com.example.petvitals.ui.components.SnackbarType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class ManageRecordViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadInitialData_inAddMode_loadsManageablePetsAndCreatesStableDraftIdentity() =
        runTest(dispatcher) {
            val petRepository = FakePetRepository(
                AppResult.Success(listOf(OWNER_PET, VIEWER_PET))
            )
            val viewModel = createViewModel(petRepository = petRepository)

            viewModel.loadInitialData(recordId = null)
            advanceUntilIdle()
            val firstState = viewModel.uiState.value
            viewModel.onAction(ManageRecordAction.OnTitleChange("Draft"))
            viewModel.loadInitialData(recordId = null)
            advanceUntilIdle()

            assertEquals(listOf(OWNER_PET), firstState.availablePets)
            assertTrue(firstState.draftRecordId.isNotBlank())
            assertTrue(firstState.createdAt > 0)
            assertEquals(firstState.draftRecordId, viewModel.uiState.value.draftRecordId)
            assertEquals(firstState.createdAt, viewModel.uiState.value.createdAt)
            assertEquals("Draft", viewModel.uiState.value.title)
            assertEquals(1, petRepository.getCurrentUserPetsCalls)
        }

    @Test
    fun loadInitialData_inEditMode_populatesRecordMetadataAndSelection() = runTest(dispatcher) {
        val record = record()
        val viewModel = createViewModel(
            petRepository = FakePetRepository(AppResult.Success(listOf(OWNER_PET, EDITOR_PET))),
            getRecords = FakeGetCurrentUserRecords(
                AppResult.Success(
                    listOf(RecordOverview(record, listOf(OWNER_PET, EDITOR_PET), canManage = true))
                )
            )
        )

        viewModel.loadInitialData(RECORD_ID)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RECORD_ID, state.draftRecordId)
        assertEquals(setOf(OWNER_PET.id, EDITOR_PET.id), state.selectedPetIds)
        assertEquals(record.petIds, state.originalPetIds)
        assertEquals(record.createdAt.time, state.createdAt)
        assertEquals(record.revision, state.revision)
        assertFalse(state.isLoading)
    }

    @Test
    fun loadInitialData_whenRecordCannotBeManaged_showsNotFoundWithoutForm() = runTest(dispatcher) {
        val viewModel = createViewModel(
            getRecords = FakeGetCurrentUserRecords(
                AppResult.Success(listOf(RecordOverview(record(), listOf(OWNER_PET), false)))
            )
        )

        viewModel.loadInitialData(RECORD_ID)
        advanceUntilIdle()

        assertEquals(R.string.record_not_found_error, viewModel.uiState.value.loadErrorMessageRes)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun retryLoad_afterFailure_loadsRecord() = runTest(dispatcher) {
        val records = FakeGetCurrentUserRecords(
            ArrayDeque(
                listOf(
                    AppResult.Failure(FirestoreError.Network),
                    AppResult.Success(
                        listOf(RecordOverview(record(), listOf(OWNER_PET, EDITOR_PET), true))
                    )
                )
            )
        )
        val viewModel = createViewModel(getRecords = records)
        viewModel.loadInitialData(RECORD_ID)
        advanceUntilIdle()
        assertEquals(R.string.network_error, viewModel.uiState.value.loadErrorMessageRes)

        viewModel.onAction(ManageRecordAction.OnRetryLoad)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.loadErrorMessageRes)
        assertEquals("Annual checkup", viewModel.uiState.value.title)
        assertEquals(2, records.calls)
    }

    @Test
    fun save_invalidForm_setsResourceErrorsWithoutCallingUseCase() = runTest(dispatcher) {
        val saveUseCase = FakeSaveRecordUseCase()
        val viewModel = createViewModel(saveUseCase = saveUseCase)
        viewModel.loadInitialData(null)
        advanceUntilIdle()
        viewModel.onAction(ManageRecordAction.OnTitleChange("x".repeat(51)))
        viewModel.onAction(ManageRecordAction.OnDescriptionChange("x".repeat(501)))

        viewModel.onAction(ManageRecordAction.OnSave("Note") {})
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(R.string.title_too_long, state.titleErrorMessageRes)
        assertEquals(
            R.string.description_cannot_be_longer_than_error,
            state.descriptionErrorMessageRes
        )
        assertEquals(R.string.select_at_least_one_pet_error, state.petSelectionErrorMessageRes)
        assertEquals(0, saveUseCase.calls)
    }

    @Test
    fun save_blankTitle_usesProvidedLocalizedFallbackAndStableIdentityAcrossRetry() =
        runTest(dispatcher) {
            val saveUseCase = FakeSaveRecordUseCase(
                ArrayDeque(
                    listOf(
                        AppResult.Failure(FirestoreError.Network),
                        AppResult.Success(Unit)
                    )
                )
            )
            val viewModel = createViewModel(saveUseCase = saveUseCase)
            viewModel.loadInitialData(null)
            advanceUntilIdle()
            viewModel.onAction(ManageRecordAction.OnPetToggle(OWNER_PET.id))
            var successCalls = 0

            viewModel.onAction(ManageRecordAction.OnSave("Localized note") { successCalls++ })
            advanceUntilIdle()
            val firstRecord = saveUseCase.savedRecords.single()
            viewModel.onAction(ManageRecordAction.OnSave("Localized note") { successCalls++ })
            advanceUntilIdle()
            val secondRecord = saveUseCase.savedRecords.last()

            assertEquals("Localized note", firstRecord.title)
            assertEquals(firstRecord.id, secondRecord.id)
            assertEquals(firstRecord.createdAt, secondRecord.createdAt)
            assertEquals(1, successCalls)
        }

    @Test
    fun save_whenTappedTwiceWhilePending_invokesUseCaseOnce() = runTest(dispatcher) {
        val saveUseCase = FakeSaveRecordUseCase()
        val viewModel = createViewModel(saveUseCase = saveUseCase)
        viewModel.loadInitialData(null)
        advanceUntilIdle()
        viewModel.onAction(ManageRecordAction.OnPetToggle(OWNER_PET.id))
        var successCalls = 0

        viewModel.onAction(ManageRecordAction.OnSave("Note") { successCalls++ })
        viewModel.onAction(ManageRecordAction.OnSave("Note") { successCalls++ })
        advanceUntilIdle()

        assertEquals(1, saveUseCase.calls)
        assertEquals(1, successCalls)
    }

    @Test
    fun save_genericFailure_emitsErrorAndPreservesDraft() = runTest(dispatcher) {
        val saveUseCase = FakeSaveRecordUseCase(
            ArrayDeque(listOf(AppResult.Failure(FirestoreError.PermissionDenied)))
        )
        val viewModel = createViewModel(saveUseCase = saveUseCase)
        viewModel.loadInitialData(null)
        advanceUntilIdle()
        viewModel.onAction(ManageRecordAction.OnTitleChange("Keep me"))
        viewModel.onAction(ManageRecordAction.OnPetToggle(OWNER_PET.id))
        val event = async { viewModel.events.first() }

        viewModel.onAction(ManageRecordAction.OnSave("Note") {})
        advanceUntilIdle()

        val snackbar = event.await() as ManageRecordEvent.OnShowSnackbar
        assertEquals(SnackbarType.ERROR, snackbar.snackbarType)
        assertEquals("Keep me", viewModel.uiState.value.title)
        assertFalse(viewModel.uiState.value.hasConflict)
    }

    @Test
    fun save_conflict_preservesDraftUntilExplicitReload() = runTest(dispatcher) {
        val oldRecord = record(title = "Original", revision = 2)
        val latestRecord = record(title = "Latest", revision = 3)
        val records = FakeGetCurrentUserRecords(
            ArrayDeque(
                listOf(
                    AppResult.Success(
                        listOf(RecordOverview(oldRecord, listOf(OWNER_PET, EDITOR_PET), true))
                    ),
                    AppResult.Success(
                        listOf(RecordOverview(latestRecord, listOf(OWNER_PET, EDITOR_PET), true))
                    )
                )
            )
        )
        val saveUseCase = FakeSaveRecordUseCase(
            ArrayDeque(listOf(AppResult.Failure(FirestoreError.Conflict)))
        )
        val viewModel = createViewModel(getRecords = records, saveUseCase = saveUseCase)
        viewModel.loadInitialData(RECORD_ID)
        advanceUntilIdle()
        viewModel.onAction(ManageRecordAction.OnTitleChange("My draft"))

        viewModel.onAction(ManageRecordAction.OnSave("Note") {})
        advanceUntilIdle()

        assertEquals("My draft", viewModel.uiState.value.title)
        assertTrue(viewModel.uiState.value.hasConflict)

        viewModel.onAction(ManageRecordAction.OnReloadLatest)
        advanceUntilIdle()

        assertEquals("Latest", viewModel.uiState.value.title)
        assertEquals(3, viewModel.uiState.value.revision)
        assertFalse(viewModel.uiState.value.hasConflict)
    }

    @Test
    fun mergeSelectedDate_usesUtcPickerDayInWesternTimeZone() {
        val losAngeles = TimeZone.getTimeZone("America/Los_Angeles")
        val currentEvent = java.util.Calendar.getInstance(losAngeles).apply {
            set(2026, java.util.Calendar.JANUARY, 10, 15, 30, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val selectedUtcMidnight = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, java.util.Calendar.FEBRUARY, 20, 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        val merged = mergeSelectedDate(currentEvent, selectedUtcMidnight, losAngeles)
        val localResult = java.util.Calendar.getInstance(losAngeles).apply { timeInMillis = merged }

        assertEquals(2026, localResult.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.FEBRUARY, localResult.get(java.util.Calendar.MONTH))
        assertEquals(20, localResult.get(java.util.Calendar.DAY_OF_MONTH))
        assertEquals(15, localResult.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(30, localResult.get(java.util.Calendar.MINUTE))
    }

    @Test
    fun eventDateToPickerDate_preservesLocalCalendarDayAcrossTimeZones() {
        listOf("America/Los_Angeles", "Pacific/Auckland").forEach { zoneId ->
            val zone = TimeZone.getTimeZone(zoneId)
            val event = java.util.Calendar.getInstance(zone).apply {
                set(2026, java.util.Calendar.JANUARY, 10, 23, 30, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis

            val pickerMillis = eventDateToPickerDate(event, zone)
            val pickerUtc = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = pickerMillis
            }

            assertEquals(2026, pickerUtc.get(java.util.Calendar.YEAR))
            assertEquals(java.util.Calendar.JANUARY, pickerUtc.get(java.util.Calendar.MONTH))
            assertEquals(10, pickerUtc.get(java.util.Calendar.DAY_OF_MONTH))
            assertEquals(0, pickerUtc.get(java.util.Calendar.HOUR_OF_DAY))
        }
    }

    @Test
    fun loadInitialData_whenRouteChanges_ignoresLateResultFromPreviousRoute() =
        runTest(dispatcher) {
            val firstRequestStarted = CompletableDeferred<Unit>()
            val releaseFirstRequest = CompletableDeferred<Unit>()
            val petRepository = object : PetRepository {
                var calls = 0

                override suspend fun getCurrentUserPets(): AppResult<FirestoreError, List<Pet>> {
                    calls++
                    return if (calls == 1) {
                        firstRequestStarted.complete(Unit)
                        withContext(NonCancellable) { releaseFirstRequest.await() }
                        AppResult.Success(listOf(OWNER_PET.copy(name = "Old route pet")))
                    } else {
                        AppResult.Success(listOf(EDITOR_PET.copy(name = "New route pet")))
                    }
                }

                override suspend fun savePet(pet: Pet) = AppResult.Success(Unit)
                override suspend fun getPetById(petId: String) = AppResult.Success(null)
                override suspend fun deletePet(petId: String) = AppResult.Success(Unit)
                override suspend fun createPetWithOwner(pet: Pet, member: Member) =
                    AppResult.Success(Unit)
            }
            val viewModel = createViewModel(petRepository = petRepository)

            viewModel.loadInitialData(null)
            dispatcher.scheduler.runCurrent()
            firstRequestStarted.await()
            viewModel.loadInitialData("new-record-id")
            dispatcher.scheduler.runCurrent()
            releaseFirstRequest.complete(Unit)
            advanceUntilIdle()

            assertEquals("new-record-id", viewModel.uiState.value.routeRecordId)
            assertEquals(
                listOf("New route pet"),
                viewModel.uiState.value.availablePets.map(Pet::name)
            )
        }

    private fun createViewModel(
        petRepository: PetRepository = FakePetRepository(AppResult.Success(listOf(OWNER_PET))),
        getRecords: GetCurrentUserRecords = FakeGetCurrentUserRecords(AppResult.Success(emptyList())),
        saveUseCase: SaveRecordUseCase = FakeSaveRecordUseCase()
    ) = ManageRecordViewModel(
        petRepository = petRepository,
        getCurrentUserRecords = getRecords,
        saveRecordUseCase = saveUseCase,
        recordValidator = RecordDataValidator()
    )

    private class FakePetRepository(
        private val result: AppResult<FirestoreError, List<Pet>>
    ) : PetRepository {
        var getCurrentUserPetsCalls = 0

        override suspend fun getCurrentUserPets(): AppResult<FirestoreError, List<Pet>> {
            getCurrentUserPetsCalls++
            return result
        }

        override suspend fun savePet(pet: Pet) = AppResult.Success(Unit)
        override suspend fun getPetById(petId: String) = AppResult.Success(null)
        override suspend fun deletePet(petId: String) = AppResult.Success(Unit)
        override suspend fun createPetWithOwner(pet: Pet, member: Member) = AppResult.Success(Unit)
    }

    private class FakeGetCurrentUserRecords(
        private val results: ArrayDeque<AppResult<FirestoreError, List<RecordOverview>>>
    ) : GetCurrentUserRecords {
        constructor(result: AppResult<FirestoreError, List<RecordOverview>>) : this(
            ArrayDeque(listOf(result))
        )

        var calls = 0

        override suspend fun invoke(): AppResult<FirestoreError, List<RecordOverview>> {
            calls++
            return results.removeFirst()
        }
    }

    private class FakeSaveRecordUseCase(
        private val results: ArrayDeque<AppResult<FirestoreError, Unit>> =
            ArrayDeque(listOf(AppResult.Success(Unit)))
    ) : SaveRecordUseCase {
        var calls = 0
        val savedRecords = mutableListOf<Record>()

        override suspend fun invoke(
            record: Record,
            previousPetIds: List<String>
        ): AppResult<FirestoreError, Unit> {
            calls++
            savedRecords += record
            return if (results.size == 1) results.first() else results.removeFirst()
        }
    }

    private companion object {
        const val RECORD_ID = "record-id"
        val OWNER_PET = Pet(
            id = "owner-pet",
            name = "Milo",
            currentUserPermission = PermissionLevel.OWNER
        )
        val EDITOR_PET = Pet(
            id = "editor-pet",
            name = "Luna",
            currentUserPermission = PermissionLevel.EDITOR
        )
        val VIEWER_PET = Pet(
            id = "viewer-pet",
            name = "Nala",
            currentUserPermission = PermissionLevel.VIEWER
        )

        fun record(
            title: String = "Annual checkup",
            revision: Long = 2
        ) = Record(
            id = RECORD_ID,
            title = title,
            petIds = listOf(OWNER_PET.id, EDITOR_PET.id),
            createdAt = Date(1_000L),
            eventDate = Date(2_000L),
            revision = revision
        )
    }
}
