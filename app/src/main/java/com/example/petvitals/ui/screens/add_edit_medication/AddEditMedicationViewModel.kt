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

    val medicationId: String? = null,
    val medicationName: String = "",
    val medicationDosage: String = "",
    val medicationFrequency: String = "",
    val isMedicationRegular: Boolean = false,
    val medicationStartDate: Long? = null,
    val medicationEndDate: Long? = null,
    val medicationNote: String = "",

    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,

    val medicationNameErrorMessage: String? = null,
    val medicationDosageErrorMessage: String? = null,
    val medicationFrequencyErrorMessage: String? = null,
    val medicationStartDateErrorMessage: String? = null,
    val medicationEndDateErrorMessage: String? = null,
    val medicationNoteErrorMessage: String? = null,
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
                            medicationId = addEditMedication.medicationId,
                            medicationName = medication.name,
                            medicationDosage = medication.dosage,
                            medicationFrequency = medication.frequency,
                            isMedicationRegular = medication.startDate == null && medication.endDate == null,
                            medicationStartDate = medication.startDate?.time,
                            medicationEndDate = medication.endDate?.time,
                            medicationNote = medication.note
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
            state.copy(isMedicationRegular = isRegular)
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
                medicationName = value,
                medicationNameErrorMessage = validateName(value)
            )
        }
    }

    private fun validateName(value: String = uiState.value.medicationName): String? {
        return when {
            value.isBlank() -> context.getString(R.string.medication_name_cannot_be_empty_error)
            value.length > 50 -> context.getString(R.string.medication_name_too_long_error)
            else -> null
        }
    }

    fun onDosageChange(value: String) {
        _uiState.update { state ->
            state.copy(
                medicationDosage = value,
                medicationDosageErrorMessage = validateDosage(value)
            )
        }
    }

    private fun validateDosage(value: String = uiState.value.medicationDosage): String? {
        return when {
            value.isBlank() -> context.getString(R.string.medication_dosage_cannot_be_empty_error)
            value.length > 50 -> context.getString(R.string.medication_dosage_too_long_error)
            else -> null
        }
    }

    fun onFrequencyChange(value: String) {
        _uiState.update { state ->
            state.copy(
                medicationFrequency = value,
                medicationFrequencyErrorMessage = validateFrequency(value)
            )
        }
    }

    private fun validateFrequency(value: String = uiState.value.medicationFrequency): String? {
        return when {
            value.isBlank() -> context.getString(R.string.medication_frequency_cannot_be_empty_error)
            value.length > 50 -> context.getString(R.string.medication_frequency_too_long_error)
            else -> null
        }
    }

    fun onStartDateChange(value: Long?) {
        _uiState.update { state ->
            state.copy(
                medicationStartDate = value,
                medicationStartDateErrorMessage = validateMedicationDates(startDate = value)
            )
        }
    }

    fun onEndDateChange(value: Long?) {
        _uiState.update { state ->
            state.copy(
                medicationEndDate = value,
                medicationStartDateErrorMessage = validateMedicationDates(endDate = value)
            )
        }
    }

    private fun validateMedicationDates(
        startDate: Long? = uiState.value.medicationStartDate,
        endDate: Long? = uiState.value.medicationEndDate
    ): String? {
        return when {
            uiState.value.isMedicationRegular -> null
            startDate != null && endDate != null && startDate > endDate -> context.getString(R.string.medication_start_date_cannot_be_after_end_date_error)
            startDate == null -> context.getString(R.string.medication_start_date_must_be_selected_error)
            else -> null
        }
    }

    fun onNoteChange(value: String) {
        _uiState.update { state ->
            state.copy(
                medicationNote = value,
                medicationNoteErrorMessage = validateNote(value)
            )
        }
    }

    private fun validateNote(value: String = uiState.value.medicationNote): String? {
        return when {
            value.length > 500 -> context.getString(R.string.medication_note_too_long_error)
            else -> null
        }
    }

    private fun isFormValid(): Boolean {

        _uiState.update {
            it.copy(
                medicationNameErrorMessage = validateName(),
                medicationDosageErrorMessage = validateDosage(),
                medicationFrequencyErrorMessage = validateFrequency(),
                medicationStartDateErrorMessage = validateMedicationDates(),
                medicationNoteErrorMessage = validateNote()
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
                name = uiState.value.medicationName,
                dosage = uiState.value.medicationDosage,
                frequency = uiState.value.medicationFrequency,
                startDate = startDate,
                endDate = endDate,
                note = uiState.value.medicationNote
            )

            val medication = when (uiState.value.medicationId) {
                null -> baseMed
                else -> baseMed.copy(id = uiState.value.medicationId!!)
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
            return when (uiState.value.isMedicationRegular) {
                true -> {
                    val startDate = null
                    val endDate = null
                    startDate to endDate
                }
                false -> {
                    val startDate = uiState.value.medicationStartDate?.let { Date(it) }
                    val endDate = uiState.value.medicationEndDate?.let { Date(it) }
                    startDate to endDate
                }
            }
    }
}
