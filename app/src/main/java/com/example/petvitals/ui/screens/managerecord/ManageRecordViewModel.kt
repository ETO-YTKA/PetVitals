package com.example.petvitals.ui.screens.managerecord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Record
import com.example.petvitals.domain.models.RecordOverview
import com.example.petvitals.domain.models.RecordType
import com.example.petvitals.domain.models.canManagePetCare
import com.example.petvitals.domain.repository.PetRepository
import com.example.petvitals.domain.usecase.GetCurrentUserRecords
import com.example.petvitals.domain.usecase.SaveRecordUseCase
import com.example.petvitals.domain.validator.RecordDataValidator
import com.example.petvitals.ui.components.SnackbarType
import com.example.petvitals.ui.utils.toMessageRes
import com.example.petvitals.ui.utils.toRecordMessageResOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ManageRecordViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val getCurrentUserRecords: GetCurrentUserRecords,
    private val saveRecordUseCase: SaveRecordUseCase,
    private val recordValidator: RecordDataValidator
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageRecordUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventChannel = Channel<ManageRecordEvent>(Channel.BUFFERED)
    val events = _eventChannel.receiveAsFlow()

    private var initialized = false
    private var loadJob: Job? = null
    private var loadGeneration = 0L

    fun loadInitialData(recordId: String?) {
        if (initialized && uiState.value.routeRecordId == recordId) return
        initialized = true

        val now = System.currentTimeMillis()
        _uiState.value = ManageRecordUiState(
            isLoading = true,
            routeRecordId = recordId,
            draftRecordId = recordId ?: UUID.randomUUID().toString(),
            eventDate = now,
            createdAt = now
        )
        loadData()
    }

    fun onAction(action: ManageRecordAction) {
        when (action) {
            is ManageRecordAction.OnTitleChange -> onTitleChange(action.title)
            is ManageRecordAction.OnTypeChange -> onTypeChange(action.type)
            ManageRecordAction.OnDatePickerToggle -> toggleDatePicker()
            ManageRecordAction.OnTimePickerToggle -> toggleTimePicker()
            is ManageRecordAction.OnDateChange -> onDateChange(action.date)
            is ManageRecordAction.OnTimeChange -> onTimeChange(action.hour, action.minute)
            is ManageRecordAction.OnDescriptionChange -> onDescriptionChange(action.description)
            ManageRecordAction.OnPetSelectorToggle -> togglePetSelector()
            is ManageRecordAction.OnPetToggle -> togglePet(action.petId)
            ManageRecordAction.OnRetryLoad -> loadData()
            ManageRecordAction.OnReloadLatest -> loadData()
            is ManageRecordAction.OnSave -> save(action.fallbackTitle, action.onSuccess)
        }
    }

    private fun loadData() {
        loadJob?.cancel()
        val generation = ++loadGeneration
        _uiState.update { it.copy(isLoading = true, loadErrorMessageRes = null) }
        val recordId = uiState.value.routeRecordId
        loadJob = viewModelScope.launch {
            when (val result = petRepository.getCurrentUserPets()) {
                is AppResult.Failure -> if (generation == loadGeneration) {
                    setLoadFailure(result.error)
                }
                is AppResult.Success -> {
                    if (generation != loadGeneration) return@launch
                    _uiState.update { state ->
                        state.copy(
                            availablePets = result.data.filter {
                                it.currentUserPermission.canManagePetCare
                            }
                        )
                    }
                    if (recordId == null) {
                        _uiState.update { it.copy(isLoading = false) }
                    } else {
                        when (val recordsResult = getCurrentUserRecords()) {
                            is AppResult.Failure -> if (generation == loadGeneration) {
                                setLoadFailure(recordsResult.error)
                            }
                            is AppResult.Success -> {
                                if (generation != loadGeneration) return@launch
                                val overview = recordsResult.data.firstOrNull {
                                    it.record.id == recordId
                                }
                                if (overview == null || !overview.canManage) {
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            loadErrorMessageRes = R.string.record_not_found_error
                                        )
                                    }
                                } else {
                                    applyRecord(overview)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun applyRecord(overview: RecordOverview) {
        val record = overview.record
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                loadErrorMessageRes = null,
                draftRecordId = record.id,
                title = record.title,
                selectedType = record.type,
                eventDate = (record.eventDate ?: record.createdAt).time,
                description = record.description.orEmpty(),
                selectedPetIds = record.petIds.toSet(),
                originalPetIds = record.petIds,
                createdAt = record.createdAt.time,
                revision = record.revision,
                hasConflict = false,
                titleErrorMessageRes = null,
                descriptionErrorMessageRes = null,
                petSelectionErrorMessageRes = null
            )
        }
    }

    private fun setLoadFailure(error: FirestoreError) {
        _uiState.update {
            it.copy(isLoading = false, loadErrorMessageRes = error.toMessageRes())
        }
    }

    private fun onTitleChange(title: String) {
        _uiState.update {
            it.copy(
                title = title,
                titleErrorMessageRes = recordValidator.validateTitle(title)
                    .toRecordMessageResOrNull()
            )
        }
    }

    private fun onTypeChange(type: RecordType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    private fun toggleDatePicker() {
        _uiState.update { it.copy(showDatePicker = !it.showDatePicker) }
    }

    private fun toggleTimePicker() {
        _uiState.update { it.copy(showTimePicker = !it.showTimePicker) }
    }

    private fun onDateChange(date: Long?) {
        if (date == null) return
        _uiState.update {
            it.copy(
                eventDate = mergeSelectedDate(it.eventDate, date),
                showTimePicker = true
            )
        }
    }

    private fun onTimeChange(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = uiState.value.eventDate
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        _uiState.update { it.copy(eventDate = calendar.timeInMillis, showTimePicker = false) }
    }

    private fun onDescriptionChange(description: String) {
        _uiState.update {
            it.copy(
                description = description,
                descriptionErrorMessageRes = recordValidator.validateDescription(description)
                    .toRecordMessageResOrNull()
            )
        }
    }

    private fun togglePetSelector() {
        _uiState.update { it.copy(showPetSelector = !it.showPetSelector) }
    }

    private fun togglePet(petId: String) {
        if (uiState.value.availablePets.none { it.id == petId }) return
        _uiState.update { state ->
            state.copy(
                selectedPetIds = if (petId in state.selectedPetIds) {
                    state.selectedPetIds - petId
                } else {
                    state.selectedPetIds + petId
                },
                petSelectionErrorMessageRes = null
            )
        }
    }

    private fun save(fallbackTitle: String, onSuccess: () -> Unit) {
        val state = uiState.value
        if (state.isSaving || state.isLoading || state.loadErrorMessageRes != null) return
        if (!validateForm(state)) return

        val record = Record(
            id = state.draftRecordId,
            title = state.title.takeIf(String::isNotBlank) ?: fallbackTitle,
            type = state.selectedType,
            eventDate = Date(state.eventDate),
            createdAt = Date(state.createdAt),
            description = state.description,
            petIds = state.selectedPetIds.toList(),
            revision = state.revision
        )
        _uiState.update { it.copy(isSaving = true, hasConflict = false) }

        viewModelScope.launch {
            try {
                when (val result = saveRecordUseCase(record, state.originalPetIds)) {
                    is AppResult.Success -> onSuccess()
                    is AppResult.Failure -> if (result.error == FirestoreError.Conflict) {
                        _uiState.update { it.copy(hasConflict = true) }
                    } else {
                        _eventChannel.send(
                            ManageRecordEvent.OnShowSnackbar(
                                messageRes = result.error.toMessageRes(),
                                snackbarType = SnackbarType.ERROR
                            )
                        )
                    }
                }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun validateForm(state: ManageRecordUiState): Boolean {
        val titleError = recordValidator.validateTitle(state.title).toRecordMessageResOrNull()
        val descriptionError = recordValidator.validateDescription(state.description)
            .toRecordMessageResOrNull()
        val petError = recordValidator.validatePetIds(state.selectedPetIds)
            .toRecordMessageResOrNull()
        _uiState.update {
            it.copy(
                titleErrorMessageRes = titleError,
                descriptionErrorMessageRes = descriptionError,
                petSelectionErrorMessageRes = petError
            )
        }
        return titleError == null && descriptionError == null && petError == null
    }
}

internal fun mergeSelectedDate(
    currentEventMillis: Long,
    selectedUtcMillis: Long,
    localTimeZone: TimeZone = TimeZone.getDefault()
): Long {
    val current = Calendar.getInstance(localTimeZone).apply {
        timeInMillis = currentEventMillis
    }
    val selected = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = selectedUtcMillis
    }
    current.set(
        selected.get(Calendar.YEAR),
        selected.get(Calendar.MONTH),
        selected.get(Calendar.DAY_OF_MONTH)
    )
    return current.timeInMillis
}

internal fun eventDateToPickerDate(
    eventMillis: Long,
    localTimeZone: TimeZone = TimeZone.getDefault()
): Long {
    val localDate = Calendar.getInstance(localTimeZone).apply {
        timeInMillis = eventMillis
    }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(
            localDate.get(Calendar.YEAR),
            localDate.get(Calendar.MONTH),
            localDate.get(Calendar.DAY_OF_MONTH)
        )
    }.timeInMillis
}
