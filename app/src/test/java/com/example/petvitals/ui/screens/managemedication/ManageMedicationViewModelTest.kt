package com.example.petvitals.ui.screens.managemedication

import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Medication
import com.example.petvitals.domain.repository.MedicationRepository
import com.example.petvitals.domain.usecase.SaveMedicationUseCase
import com.example.petvitals.domain.validator.MedicationDataValidator
import com.example.petvitals.ui.components.SnackbarType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class ManageMedicationViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadInitialData_inAddMode_storesRouteWithoutReading() = runTest(dispatcher) {
        val repository = FakeMedicationRepository()
        val viewModel = createViewModel(repository)

        viewModel.loadInitialData(PET_ID, medicationId = null)

        assertEquals(PET_ID, viewModel.uiState.value.petId)
        assertNull(viewModel.uiState.value.medicationId)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(0, repository.getCalls)
    }

    @Test
    fun loadInitialData_whenRouteIsAlreadyInitialized_preservesDraft() = runTest(dispatcher) {
        val repository = FakeMedicationRepository()
        val viewModel = createViewModel(repository)
        viewModel.loadInitialData(PET_ID, medicationId = null)
        viewModel.onAction(ManageMedicationAction.OnNameChange("Draft medication"))

        viewModel.loadInitialData(PET_ID, medicationId = null)

        assertEquals("Draft medication", viewModel.uiState.value.name)
        assertEquals(0, repository.getCalls)
    }

    @Test
    fun loadInitialData_inEditMode_populatesMedicationAndPreservesRouteId() =
        runTest(dispatcher) {
            val medication = medication(
                startDate = Date(START_DATE),
                endDate = Date(END_DATE)
            )
            val repository = FakeMedicationRepository(
                getResults = ArrayDeque(listOf(AppResult.Success(medication)))
            )
            val viewModel = createViewModel(repository)

            viewModel.loadInitialData(PET_ID, MEDICATION_ID)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(MEDICATION_ID, state.medicationId)
            assertEquals("Antibiotic", state.name)
            assertEquals(START_DATE, state.startDate)
            assertEquals(END_DATE, state.endDate)
            assertFalse(state.isRegular)
            assertFalse(state.isLoading)
        }

    @Test
    fun loadInitialData_whenMedicationIsMissing_showsNotFoundError() = runTest(dispatcher) {
        val repository = FakeMedicationRepository(
            getResults = ArrayDeque(listOf(AppResult.Success(null)))
        )
        val viewModel = createViewModel(repository)

        viewModel.loadInitialData(PET_ID, MEDICATION_ID)
        advanceUntilIdle()

        assertEquals(R.string.medication_not_found_error, viewModel.uiState.value.loadErrorMessageRes)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun retryLoad_afterFailure_loadsMedication() = runTest(dispatcher) {
        val repository = FakeMedicationRepository(
            getResults = ArrayDeque(
                listOf(
                    AppResult.Failure(FirestoreError.Network),
                    AppResult.Success(medication())
                )
            )
        )
        val viewModel = createViewModel(repository)

        viewModel.loadInitialData(PET_ID, MEDICATION_ID)
        advanceUntilIdle()
        assertEquals(R.string.network_error, viewModel.uiState.value.loadErrorMessageRes)

        viewModel.onAction(ManageMedicationAction.OnRetryLoad)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.loadErrorMessageRes)
        assertEquals("Antibiotic", viewModel.uiState.value.name)
        assertEquals(2, repository.getCalls)
    }

    @Test
    fun changingScheduleMode_clearsAndRestoresScheduleValidation() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeMedicationRepository())
        viewModel.loadInitialData(PET_ID, medicationId = null)

        viewModel.onAction(ManageMedicationAction.OnStartDateChange(2L))
        viewModel.onAction(ManageMedicationAction.OnEndDateChange(1L))
        assertEquals(
            R.string.medication_start_date_cannot_be_after_end_date_error,
            viewModel.uiState.value.scheduleErrorMessageRes
        )

        viewModel.onAction(ManageMedicationAction.OnRegularChange(true))
        assertNull(viewModel.uiState.value.scheduleErrorMessageRes)
        assertEquals(2L, viewModel.uiState.value.startDate)
        assertEquals(1L, viewModel.uiState.value.endDate)

        viewModel.onAction(ManageMedicationAction.OnRegularChange(false))
        assertEquals(
            R.string.medication_start_date_cannot_be_after_end_date_error,
            viewModel.uiState.value.scheduleErrorMessageRes
        )
    }

    @Test
    fun endDate_canBeClearedForOpenEndedDateRange() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeMedicationRepository())
        viewModel.loadInitialData(PET_ID, medicationId = null)
        viewModel.onAction(ManageMedicationAction.OnStartDateChange(1_000L))
        viewModel.onAction(ManageMedicationAction.OnEndDateChange(2_000L))

        viewModel.onAction(ManageMedicationAction.OnEndDateChange(null))

        assertEquals(1_000L, viewModel.uiState.value.startDate)
        assertNull(viewModel.uiState.value.endDate)
        assertNull(viewModel.uiState.value.scheduleErrorMessageRes)
    }

    @Test
    fun save_regularMedication_generatesIdAndClearsPersistedDates() = runTest(dispatcher) {
        val saveUseCase = FakeSaveMedicationUseCase()
        val viewModel = createViewModel(FakeMedicationRepository(), saveUseCase)
        viewModel.loadInitialData(PET_ID, medicationId = null)
        enterValidMedication(viewModel)
        viewModel.onAction(ManageMedicationAction.OnStartDateChange(START_DATE))
        viewModel.onAction(ManageMedicationAction.OnEndDateChange(END_DATE))
        viewModel.onAction(ManageMedicationAction.OnRegularChange(true))

        viewModel.onAction(ManageMedicationAction.OnSave {})
        advanceUntilIdle()

        assertTrue(saveUseCase.savedMedication?.id?.isNotBlank() == true)
        assertEquals(PET_ID, saveUseCase.savedMedication?.petId)
        assertNull(saveUseCase.savedMedication?.startDate)
        assertNull(saveUseCase.savedMedication?.endDate)
    }

    @Test
    fun save_whenTappedTwiceWhilePending_invokesUseCaseAndSuccessOnce() = runTest(dispatcher) {
        val saveUseCase = FakeSaveMedicationUseCase()
        val viewModel = createViewModel(FakeMedicationRepository(), saveUseCase)
        viewModel.loadInitialData(PET_ID, medicationId = null)
        enterValidMedication(viewModel)
        viewModel.onAction(ManageMedicationAction.OnRegularChange(true))
        var successCalls = 0

        viewModel.onAction(ManageMedicationAction.OnSave { successCalls++ })
        viewModel.onAction(ManageMedicationAction.OnSave { successCalls++ })
        advanceUntilIdle()

        assertEquals(1, saveUseCase.calls)
        assertEquals(1, successCalls)
    }

    @Test
    fun save_inEditMode_preservesIdAndDates() = runTest(dispatcher) {
        val saveUseCase = FakeSaveMedicationUseCase()
        val repository = FakeMedicationRepository(
            getResults = ArrayDeque(
                listOf(
                    AppResult.Success(
                        medication(Date(START_DATE), Date(END_DATE))
                    )
                )
            )
        )
        val viewModel = createViewModel(repository, saveUseCase)
        viewModel.loadInitialData(PET_ID, MEDICATION_ID)
        advanceUntilIdle()

        viewModel.onAction(ManageMedicationAction.OnSave {})
        advanceUntilIdle()

        assertEquals(MEDICATION_ID, saveUseCase.savedMedication?.id)
        assertEquals(Date(START_DATE), saveUseCase.savedMedication?.startDate)
        assertEquals(Date(END_DATE), saveUseCase.savedMedication?.endDate)
    }

    @Test
    fun save_whenUseCaseFails_emitsErrorWithoutInvokingSuccess() = runTest(dispatcher) {
        val saveUseCase = FakeSaveMedicationUseCase(
            result = AppResult.Failure(FirestoreError.PermissionDenied)
        )
        val viewModel = createViewModel(FakeMedicationRepository(), saveUseCase)
        viewModel.loadInitialData(PET_ID, medicationId = null)
        enterValidMedication(viewModel)
        viewModel.onAction(ManageMedicationAction.OnRegularChange(true))
        var successCalls = 0
        val event = async { viewModel.events.first() }

        viewModel.onAction(ManageMedicationAction.OnSave { successCalls++ })
        advanceUntilIdle()

        val snackbar = event.await() as ManageMedicationEvent.OnShowSnackbar
        assertEquals(R.string.something_went_wrong_error, snackbar.messageRes)
        assertEquals(SnackbarType.ERROR, snackbar.snackbarType)
        assertEquals(0, successCalls)
    }

    private fun createViewModel(
        repository: MedicationRepository,
        saveUseCase: SaveMedicationUseCase = FakeSaveMedicationUseCase()
    ) = ManageMedicationViewModel(
        medicationRepository = repository,
        saveMedicationUseCase = saveUseCase,
        medicationValidator = MedicationDataValidator()
    )

    private fun enterValidMedication(viewModel: ManageMedicationViewModel) {
        viewModel.onAction(ManageMedicationAction.OnNameChange("Antibiotic"))
        viewModel.onAction(ManageMedicationAction.OnDosageChange("1 tablet"))
        viewModel.onAction(ManageMedicationAction.OnFrequencyChange("Twice daily"))
    }

    private class FakeMedicationRepository(
        private val getResults: ArrayDeque<AppResult<FirestoreError, Medication?>> = ArrayDeque()
    ) : MedicationRepository {
        var getCalls = 0

        override suspend fun getMedications(
            petId: String
        ): AppResult<FirestoreError, List<Medication>> = AppResult.Success(emptyList())

        override suspend fun saveMedication(
            medication: Medication
        ): AppResult<FirestoreError, Unit> = AppResult.Success(Unit)

        override suspend fun deleteMedication(
            medication: Medication
        ): AppResult<FirestoreError, Unit> = AppResult.Success(Unit)

        override suspend fun getMedicationById(
            petId: String,
            medicationId: String
        ): AppResult<FirestoreError, Medication?> {
            getCalls++
            return getResults.removeFirst()
        }
    }

    private class FakeSaveMedicationUseCase(
        private val result: AppResult<FirestoreError, Unit> = AppResult.Success(Unit)
    ) : SaveMedicationUseCase {
        var calls = 0
        var savedMedication: Medication? = null

        override suspend fun invoke(
            medication: Medication
        ): AppResult<FirestoreError, Unit> {
            calls++
            savedMedication = medication
            return result
        }
    }

    private companion object {
        const val PET_ID = "pet-id"
        const val MEDICATION_ID = "medication-id"
        const val START_DATE = 1_000L
        const val END_DATE = 2_000L

        fun medication(
            startDate: Date? = null,
            endDate: Date? = null
        ) = Medication(
            id = MEDICATION_ID,
            petId = PET_ID,
            name = "Antibiotic",
            dosage = "1 tablet",
            frequency = "Twice daily",
            startDate = startDate,
            endDate = endDate
        )
    }
}
