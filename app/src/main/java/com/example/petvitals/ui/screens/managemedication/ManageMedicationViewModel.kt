package com.example.petvitals.ui.screens.managemedication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.models.Medication
import com.example.petvitals.domain.repository.MedicationRepository
import com.example.petvitals.domain.usecase.SaveMedicationUseCase
import com.example.petvitals.domain.validator.MedicationDataValidator
import com.example.petvitals.ui.components.SnackbarType
import com.example.petvitals.ui.utils.toMedicationMessageResOrNull
import com.example.petvitals.ui.utils.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

@HiltViewModel
class ManageMedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val saveMedicationUseCase: SaveMedicationUseCase,
    private val medicationValidator: MedicationDataValidator
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageMedicationUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventChannel = Channel<ManageMedicationEvent>()
    val events = _eventChannel.receiveAsFlow()

    private var isSaving = false

    fun onAction(action: ManageMedicationAction) {
        when (action) {
            is ManageMedicationAction.OnNameChange -> onNameChange(action.name)
            is ManageMedicationAction.OnDosageChange -> onDosageChange(action.dosage)
            is ManageMedicationAction.OnFrequencyChange -> onFrequencyChange(action.frequency)
            is ManageMedicationAction.OnRegularChange -> onRegularChange(action.isRegular)
            ManageMedicationAction.OnStartDatePickerToggle -> toggleStartDatePicker()
            ManageMedicationAction.OnEndDatePickerToggle -> toggleEndDatePicker()
            is ManageMedicationAction.OnStartDateChange -> onStartDateChange(action.date)
            is ManageMedicationAction.OnEndDateChange -> onEndDateChange(action.date)
            is ManageMedicationAction.OnNoteChange -> onNoteChange(action.note)
            ManageMedicationAction.OnRetryLoad -> loadMedication()
            is ManageMedicationAction.OnSave -> onSave(action.onSuccess)
        }
    }

    fun loadInitialData(petId: String, medicationId: String?) {
        val currentState = uiState.value
        if (
            currentState.petId.isNotBlank() &&
            currentState.petId == petId &&
            currentState.medicationId == medicationId
        ) {
            return
        }

        _uiState.value = ManageMedicationUiState(
            isLoading = medicationId != null,
            petId = petId,
            medicationId = medicationId
        )

        if (medicationId != null) loadMedication()
    }

    private fun loadMedication() {
        val petId = uiState.value.petId
        val medicationId = uiState.value.medicationId ?: return

        _uiState.update { state ->
            state.copy(isLoading = true, loadErrorMessageRes = null)
        }

        viewModelScope.launch {
            when (val result = medicationRepository.getMedicationById(petId, medicationId)) {
                is AppResult.Failure -> _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        loadErrorMessageRes = result.error.toMessageRes()
                    )
                }
                is AppResult.Success -> {
                    val medication = result.data
                    if (medication == null) {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                loadErrorMessageRes = R.string.medication_not_found_error
                            )
                        }
                    } else {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                loadErrorMessageRes = null,
                                name = medication.name,
                                dosage = medication.dosage,
                                frequency = medication.frequency,
                                isRegular = medication.startDate == null && medication.endDate == null,
                                startDate = medication.startDate?.time,
                                endDate = medication.endDate?.time,
                                note = medication.note
                            )
                        }
                    }
                }
            }
        }
    }

    private fun onNameChange(name: String) {
        _uiState.update { state ->
            state.copy(
                name = name,
                nameErrorMessageRes = medicationValidator.validateName(name)
                    .toMedicationMessageResOrNull()
            )
        }
    }

    private fun onDosageChange(dosage: String) {
        _uiState.update { state ->
            state.copy(
                dosage = dosage,
                dosageErrorMessageRes = medicationValidator.validateDosage(dosage)
                    .toMedicationMessageResOrNull()
            )
        }
    }

    private fun onFrequencyChange(frequency: String) {
        _uiState.update { state ->
            state.copy(
                frequency = frequency,
                frequencyErrorMessageRes = medicationValidator.validateFrequency(frequency)
                    .toMedicationMessageResOrNull()
            )
        }
    }

    private fun onRegularChange(isRegular: Boolean) {
        val state = uiState.value
        _uiState.update {
            it.copy(
                isRegular = isRegular,
                scheduleErrorMessageRes = if (isRegular) {
                    null
                } else {
                    validateSchedule(
                        isRegular = false,
                        startDate = state.startDate,
                        endDate = state.endDate
                    )
                }
            )
        }
    }

    private fun toggleStartDatePicker() {
        _uiState.update { state ->
            state.copy(showStartDatePicker = !state.showStartDatePicker)
        }
    }

    private fun toggleEndDatePicker() {
        _uiState.update { state ->
            state.copy(showEndDatePicker = !state.showEndDatePicker)
        }
    }

    private fun onStartDateChange(startDate: Long?) {
        val state = uiState.value
        _uiState.update {
            it.copy(
                startDate = startDate,
                scheduleErrorMessageRes = validateSchedule(
                    isRegular = state.isRegular,
                    startDate = startDate,
                    endDate = state.endDate
                )
            )
        }
    }

    private fun onEndDateChange(endDate: Long?) {
        val state = uiState.value
        _uiState.update {
            it.copy(
                endDate = endDate,
                scheduleErrorMessageRes = validateSchedule(
                    isRegular = state.isRegular,
                    startDate = state.startDate,
                    endDate = endDate
                )
            )
        }
    }

    private fun onNoteChange(note: String) {
        _uiState.update { state ->
            state.copy(
                note = note,
                noteErrorMessageRes = medicationValidator.validateNote(note)
                    .toMedicationMessageResOrNull()
            )
        }
    }

    private fun onSave(onSuccess: () -> Unit) {
        if (isSaving || !isFormValid()) return

        val state = uiState.value
        val medication = Medication(
            id = state.medicationId ?: UUID.randomUUID().toString(),
            petId = state.petId,
            name = state.name,
            dosage = state.dosage,
            frequency = state.frequency,
            startDate = if (state.isRegular) null else state.startDate?.let(::Date),
            endDate = if (state.isRegular) null else state.endDate?.let(::Date),
            note = state.note
        )

        isSaving = true
        viewModelScope.launch {
            try {
                when (val result = saveMedicationUseCase(medication)) {
                    is AppResult.Success -> onSuccess()
                    is AppResult.Failure -> _eventChannel.send(
                        ManageMedicationEvent.OnShowSnackbar(
                            messageRes = result.error.toMessageRes(),
                            snackbarType = SnackbarType.ERROR
                        )
                    )
                }
            } finally {
                isSaving = false
            }
        }
    }

    private fun isFormValid(): Boolean {
        val state = uiState.value
        val nameError = medicationValidator.validateName(state.name)
            .toMedicationMessageResOrNull()
        val dosageError = medicationValidator.validateDosage(state.dosage)
            .toMedicationMessageResOrNull()
        val frequencyError = medicationValidator.validateFrequency(state.frequency)
            .toMedicationMessageResOrNull()
        val scheduleError = validateSchedule(state.isRegular, state.startDate, state.endDate)
        val noteError = medicationValidator.validateNote(state.note)
            .toMedicationMessageResOrNull()

        _uiState.update {
            it.copy(
                nameErrorMessageRes = nameError,
                dosageErrorMessageRes = dosageError,
                frequencyErrorMessageRes = frequencyError,
                scheduleErrorMessageRes = scheduleError,
                noteErrorMessageRes = noteError
            )
        }

        return listOf(
            nameError,
            dosageError,
            frequencyError,
            scheduleError,
            noteError
        ).all { it == null }
    }

    private fun validateSchedule(
        isRegular: Boolean,
        startDate: Long?,
        endDate: Long?
    ): Int? = medicationValidator.validateSchedule(isRegular, startDate, endDate)
        .toMedicationMessageResOrNull()
}