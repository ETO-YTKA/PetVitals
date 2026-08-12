package com.example.petvitals.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.models.RecordType
import com.example.petvitals.domain.usecase.DeleteRecordUseCase
import com.example.petvitals.domain.usecase.GetCurrentUserRecords
import com.example.petvitals.ui.utils.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val getCurrentUserRecords: GetCurrentUserRecords,
    private val deleteRecordUseCase: DeleteRecordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState = _uiState.asStateFlow()

    private val eventChannel = Channel<RecordsEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        loadRecords(isInitialLoad = true)
    }

    fun onAction(action: RecordsAction) {
        when (action) {
            is RecordsAction.OnSearchQueryChange -> {
                _uiState.update { it.copy(searchQuery = action.query) }
            }
            RecordsAction.OnClearSearch -> {
                _uiState.update { it.copy(searchQuery = "") }
            }
            RecordsAction.OnClearFilters -> {
                _uiState.update {
                    it.copy(
                        searchQuery = "",
                        selectedPetIds = emptySet(),
                        selectedTypeFilters = emptySet()
                    )
                }
            }
            RecordsAction.OnRefresh -> loadRecords(isInitialLoad = false)
            is RecordsAction.OnPetFilterToggle -> togglePetFilter(action.petId)
            is RecordsAction.OnTypeFilterToggle -> toggleTypeFilter(action.type)
            is RecordsAction.OnRecordExpansionToggle -> toggleExpansion(action.recordId)
            is RecordsAction.OnDeleteRecordClick -> deleteRecord(action.recordId)
        }
    }

    private fun loadRecords(isInitialLoad: Boolean) {
        if (
            !isInitialLoad &&
            (
                _uiState.value.isInitialLoading ||
                    _uiState.value.isRefreshing ||
                    _uiState.value.deletingRecordId != null
            )
        ) {
            return
        }

        _uiState.update { state ->
            state.copy(
                isInitialLoading = isInitialLoad,
                isRefreshing = !isInitialLoad,
                errorMessageRes = if (isInitialLoad) null else state.errorMessageRes
            )
        }

        viewModelScope.launch {
            when (val result = getCurrentUserRecords()) {
                is AppResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            errorMessageRes = null,
                            records = result.data
                        )
                    }
                }
                is AppResult.Failure -> {
                    if (isInitialLoad) {
                        _uiState.update { state ->
                            state.copy(
                                isInitialLoading = false,
                                errorMessageRes = result.error.toMessageRes()
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isRefreshing = false) }
                        eventChannel.send(RecordsEvent.OnShowError(result.error.toMessageRes()))
                    }
                }
            }
        }
    }

    private fun deleteRecord(recordId: String) {
        val state = _uiState.value
        val overview = state.records.firstOrNull { it.record.id == recordId } ?: return
        if (!overview.canManage || state.deletingRecordId != null || state.isRefreshing) return

        _uiState.update { it.copy(deletingRecordId = recordId) }

        viewModelScope.launch {
            when (val result = deleteRecordUseCase(overview.record)) {
                is AppResult.Success -> {
                    _uiState.update { current ->
                        current.copy(
                            records = current.records.filterNot { it.record.id == recordId },
                            expandedRecordIds = current.expandedRecordIds - recordId
                        )
                    }
                }
                is AppResult.Failure -> {
                    eventChannel.send(RecordsEvent.OnShowError(result.error.toMessageRes()))
                }
            }

            _uiState.update { current ->
                if (current.deletingRecordId == recordId) {
                    current.copy(deletingRecordId = null)
                } else {
                    current
                }
            }
        }
    }

    private fun togglePetFilter(petId: String) {
        _uiState.update { state ->
            state.copy(
                selectedPetIds = state.selectedPetIds.toggle(petId)
            )
        }
    }

    private fun toggleTypeFilter(type: RecordType) {
        _uiState.update { state ->
            state.copy(
                selectedTypeFilters = state.selectedTypeFilters.toggle(type)
            )
        }
    }

    private fun toggleExpansion(recordId: String) {
        _uiState.update { state ->
            state.copy(
                expandedRecordIds = state.expandedRecordIds.toggle(recordId)
            )
        }
    }
}

private fun <T> Set<T>.toggle(value: T): Set<T> =
    if (value in this) this - value else this + value
