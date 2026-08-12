package com.example.petvitals.ui.screens.records

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.Record
import com.example.petvitals.domain.models.RecordOverview
import com.example.petvitals.domain.models.RecordType
import com.example.petvitals.domain.usecase.DeleteRecordUseCase
import com.example.petvitals.domain.usecase.GetCurrentUserRecords
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordsViewModelTest {

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
    fun initialLoad_exposesRecordOverviewsAndStopsLoading() = runTest(dispatcher) {
        val pet = Pet(id = "pet-1", name = "Mochi")
        val overview = RecordOverview(
            record = Record(id = "record-1", petIds = listOf(pet.id)),
            pets = listOf(pet),
            canManage = true
        )
        val viewModel = RecordsViewModel(
            getCurrentUserRecords = FakeGetCurrentUserRecords(
                AppResult.Success(listOf(overview))
            ),
            deleteRecordUseCase = FakeDeleteRecordUseCase()
        )

        advanceUntilIdle()

        assertEquals(listOf(overview), viewModel.uiState.value.records)
        assertFalse(viewModel.uiState.value.isInitialLoading)
    }

    @Test
    fun actions_updateOnlyUserControlledState() = runTest(dispatcher) {
        val viewModel = RecordsViewModel(
            getCurrentUserRecords = FakeGetCurrentUserRecords(
                AppResult.Success(emptyList())
            ),
            deleteRecordUseCase = FakeDeleteRecordUseCase()
        )
        advanceUntilIdle()

        viewModel.onAction(RecordsAction.OnSearchQueryChange("vaccine"))
        viewModel.onAction(RecordsAction.OnPetFilterToggle("pet-1"))
        viewModel.onAction(RecordsAction.OnTypeFilterToggle(RecordType.VACCINATION))
        viewModel.onAction(RecordsAction.OnRecordExpansionToggle("record-1"))

        val state = viewModel.uiState.value
        assertEquals("vaccine", state.searchQuery)
        assertEquals(setOf("pet-1"), state.selectedPetIds)
        assertEquals(
            setOf(RecordType.VACCINATION),
            state.selectedTypeFilters
        )
        assertEquals(setOf("record-1"), state.expandedRecordIds)
    }

    @Test
    fun clearFilters_resetsSearchPetAndTypeFilters() = runTest(dispatcher) {
        val viewModel = RecordsViewModel(
            getCurrentUserRecords = FakeGetCurrentUserRecords(AppResult.Success(emptyList())),
            deleteRecordUseCase = FakeDeleteRecordUseCase()
        )
        advanceUntilIdle()
        viewModel.onAction(RecordsAction.OnSearchQueryChange("vaccine"))
        viewModel.onAction(RecordsAction.OnPetFilterToggle("pet-1"))
        viewModel.onAction(RecordsAction.OnTypeFilterToggle(RecordType.VACCINATION))

        viewModel.onAction(RecordsAction.OnClearFilters)

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertTrue(state.selectedPetIds.isEmpty())
        assertTrue(state.selectedTypeFilters.isEmpty())
    }

    @Test
    fun deleteViewerRecord_isRejectedByViewModelGuard() = runTest(dispatcher) {
        val record = Record(id = "record-1", petIds = listOf("pet-1"))
        val deleteUseCase = FakeDeleteRecordUseCase()
        val viewModel = RecordsViewModel(
            getCurrentUserRecords = FakeGetCurrentUserRecords(
                AppResult.Success(
                    listOf(RecordOverview(record, emptyList(), canManage = false))
                )
            ),
            deleteRecordUseCase = deleteUseCase
        )
        advanceUntilIdle()

        viewModel.onAction(RecordsAction.OnDeleteRecordClick(record.id))
        advanceUntilIdle()

        assertEquals(0, deleteUseCase.calls)
        assertTrue(viewModel.uiState.value.records.any { it.record.id == record.id })
    }

    @Test
    fun successfulDelete_removesRecordOnlyAfterUseCaseCompletes() = runTest(dispatcher) {
        val record = Record(id = "record-1", petIds = listOf("pet-1"))
        val completion = CompletableDeferred<AppResult<FirestoreError, Unit>>()
        val deleteUseCase = FakeDeleteRecordUseCase { completion.await() }
        val viewModel = RecordsViewModel(
            getCurrentUserRecords = FakeGetCurrentUserRecords(
                AppResult.Success(
                    listOf(RecordOverview(record, emptyList(), canManage = true))
                )
            ),
            deleteRecordUseCase = deleteUseCase
        )
        advanceUntilIdle()

        viewModel.onAction(RecordsAction.OnDeleteRecordClick(record.id))
        runCurrent()

        assertTrue(viewModel.uiState.value.records.any { it.record.id == record.id })
        assertEquals(record.id, viewModel.uiState.value.deletingRecordId)

        completion.complete(AppResult.Success(Unit))
        advanceUntilIdle()

        assertEquals(1, deleteUseCase.calls)
        assertTrue(viewModel.uiState.value.records.isEmpty())
    }

    @Test
    fun refreshWhileDeleting_isIgnoredToPreventStaleRecordReload() = runTest(dispatcher) {
        val record = Record(id = "record-1", petIds = listOf("pet-1"))
        val overview = RecordOverview(record, emptyList(), canManage = true)
        val loadUseCase = FakeGetCurrentUserRecords(AppResult.Success(listOf(overview)))
        val deleteCompletion = CompletableDeferred<AppResult<FirestoreError, Unit>>()
        val viewModel = RecordsViewModel(
            getCurrentUserRecords = loadUseCase,
            deleteRecordUseCase = FakeDeleteRecordUseCase { deleteCompletion.await() }
        )
        advanceUntilIdle()

        viewModel.onAction(RecordsAction.OnDeleteRecordClick(record.id))
        runCurrent()
        viewModel.onAction(RecordsAction.OnRefresh)
        runCurrent()

        assertEquals(1, loadUseCase.calls)

        deleteCompletion.complete(AppResult.Success(Unit))
        advanceUntilIdle()
    }

    private class FakeGetCurrentUserRecords(
        var result: AppResult<FirestoreError, List<RecordOverview>>
    ) : GetCurrentUserRecords {
        var calls = 0

        override suspend fun invoke(): AppResult<FirestoreError, List<RecordOverview>> {
            calls++
            return result
        }
    }

    private class FakeDeleteRecordUseCase(
        private val result: suspend () -> AppResult<FirestoreError, Unit> = {
            AppResult.Success(Unit)
        }
    ) : DeleteRecordUseCase {
        var calls = 0

        override suspend fun invoke(record: Record): AppResult<FirestoreError, Unit> {
            calls++
            return result()
        }
    }
}
