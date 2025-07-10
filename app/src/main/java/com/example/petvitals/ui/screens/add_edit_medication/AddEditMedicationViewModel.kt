package com.example.petvitals.ui.screens.add_edit_medication

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.AddEditMedication
import com.example.petvitals.R
import com.example.petvitals.data.repository.medication.Medication
import com.example.petvitals.data.repository.medication.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class AddEditMedicationUiState(
    val isLoading: Boolean = false,

    val petId: String = "",

    val id: String? = null,
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val isRegular: Boolean = false,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val note: String = "",

    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,

    val nameErrorMessage: String? = null,
    val dosageErrorMessage: String? = null,
    val frequencyErrorMessage: String? = null,
    val startDateErrorMessage: String? = null,
    val endDateErrorMessage: String? = null,
    val noteErrorMessage: String? = null,
)

@HiltViewModel
class AddEditMedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddEditMedicationUiState())
    val uiState = _uiState.asStateFlow()

    fun loadInitialData(addEditMedication: AddEditMedication) {
        _uiState.update { state ->
            state.copy(isLoading = true)
        }
        val medicationId = addEditMedication.medicationId
        val petId = addEditMedication.petId

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(petId = addEditMedication.petId)
            }

            medicationId?.let {
                val medication = medicationRepository.getMedicationById(
                    medicationId = medicationId,
                    petId = petId
                )
                medication?.let {
                    _uiState.update { state ->
                        state.copy(
                            id = addEditMedication.medicationId,
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

        _uiState.update { state ->
            state.copy(isLoading = false)
        }
    }

    fun toggleRegularMedication(isRegular: Boolean) {
        _uiState.update { state ->
            state.copy(isRegular = isRegular)
        }
    }

    fun toggleStartDatePicker() {
        _uiState.update { state ->
            state.copy(showStartDatePicker = !state.showStartDatePicker)
        }
    }

    fun toggleEndDatePicker() {
        _uiState.update { state ->
            state.copy(showEndDatePicker = !state.showEndDatePicker)
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { state ->
            state.copy(
                name = value,
                nameErrorMessage = validateName(value)
            )
        }
    }

    private fun validateName(value: String = uiState.value.name): String? {
        return when {
            value.isBlank() -> context.getString(R.string.medication_name_cannot_be_empty_error)
            value.length > 50 -> context.getString(R.string.medication_name_too_long_error)
            else -> null
        }
    }

    fun onDosageChange(value: String) {
        _uiState.update { state ->
            state.copy(
                dosage = value,
                dosageErrorMessage = validateDosage(value)
            )
        }
    }

    private fun validateDosage(value: String = uiState.value.dosage): String? {
        return when {
            value.isBlank() -> context.getString(R.string.medication_dosage_cannot_be_empty_error)
            value.length > 50 -> context.getString(R.string.medication_dosage_too_long_error)
            else -> null
        }
    }

    fun onFrequencyChange(value: String) {
        _uiState.update { state ->
            state.copy(
                frequency = value,
                frequencyErrorMessage = validateFrequency(value)
            )
        }
    }

    private fun validateFrequency(value: String = uiState.value.frequency): String? {
        return when {
            value.isBlank() -> context.getString(R.string.medication_frequency_cannot_be_empty_error)
            value.length > 50 -> context.getString(R.string.medication_frequency_too_long_error)
            else -> null
        }
    }

    fun onStartDateChange(value: Long?) {
        _uiState.update { state ->
            state.copy(
                startDate = value,
                startDateErrorMessage = validateMedicationDates(startDate = value)
            )
        }
    }

    fun onEndDateChange(value: Long?) {
        _uiState.update { state ->
            state.copy(
                endDate = value,
                startDateErrorMessage = validateMedicationDates(endDate = value)
            )
        }
    }

    private fun validateMedicationDates(
        startDate: Long? = uiState.value.startDate,
        endDate: Long? = uiState.value.endDate
    ): String? {
        return when {
            uiState.value.isRegular -> null
            startDate != null && endDate != null && startDate > endDate -> context.getString(R.string.medication_start_date_cannot_be_after_end_date_error)
            startDate == null -> context.getString(R.string.medication_start_date_must_be_selected_error)
            else -> null
        }
    }

    fun onNoteChange(value: String) {
        _uiState.update { state ->
            state.copy(
                note = value,
                noteErrorMessage = validateNote(value)
            )
        }
    }

    private fun validateNote(value: String = uiState.value.note): String? {
        return when {
            value.length > 500 -> context.getString(R.string.medication_note_too_long_error)
            else -> null
        }
    }

    private fun isFormValid(): Boolean {

        _uiState.update {
            it.copy(
                nameErrorMessage = validateName(),
                dosageErrorMessage = validateDosage(),
                frequencyErrorMessage = validateFrequency(),
                startDateErrorMessage = validateMedicationDates(),
                noteErrorMessage = validateNote()
            )
        }

        return validateMedicationDates() == null
                && validateName() == null
                && validateDosage() == null
                && validateFrequency() == null
                && validateNote() == null
    }

    fun onSaveClick(onSuccess: () -> Unit) {
        if (!isFormValid()) return

        viewModelScope.launch {
            val (startDate, endDate) = getDates()

            val baseMed = Medication(
                petId = uiState.value.petId,
                name = uiState.value.name,
                dosage = uiState.value.dosage,
                frequency = uiState.value.frequency,
                startDate = startDate,
                endDate = endDate,
                note = uiState.value.note
            )

            val medication = when (uiState.value.id) {
                null -> baseMed
                else -> baseMed.copy(id = uiState.value.id!!)
            }

            try {
                medicationRepository.saveMedication(medication)
                Toast.makeText(
                    context,
                    context.getString(R.string.medication_saved_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                onSuccess()
            } catch (e: Exception) {
                Log.d("AddEditMedicationViewModel", e.message.orEmpty())
                Toast.makeText(
                    context,
                    context.getString(R.string.something_went_wrong_please_try_again_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun getDates(): Pair<Date?, Date?> {
            return when (uiState.value.isRegular) {
                true -> {
                    val startDate = null
                    val endDate = null
                    startDate to endDate
                }
                false -> {
                    val startDate = uiState.value.startDate?.let { Date(it) }
                    val endDate = uiState.value.endDate?.let { Date(it) }
                    startDate to endDate
                }
            }
    }
}
