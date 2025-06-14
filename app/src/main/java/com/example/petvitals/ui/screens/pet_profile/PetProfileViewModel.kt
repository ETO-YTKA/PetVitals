package com.example.petvitals.ui.screens.pet_profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.repository.medication.Medication
import com.example.petvitals.data.repository.medication.MedicationRepository
import com.example.petvitals.data.repository.pet.DobPrecision
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PetProfileUiState(
    val pet: Pet = Pet(),
    val dob: String = "",
    val age: String = "",
    val medications: List<Medication> = emptyList(),
    val updatedHealthNote: String = "",
    val updatedFoodNote: String = "",

    val medicationName: String = "",
    val medicationDosage: String = "",
    val medicationFrequency: String = "",
    val isMedicationRegular: Boolean = false,
    val medicationStartDateValue: String? = null,
    val medicationEndDateValue: String? = null,
    val medicationNote: String = "",

    val medicationStartDate: Long? = null,
    val medicationEndDate: Long? = null,

    val showMedicationModal: Boolean = false,
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    val isHealthNoteInEditMode: Boolean = false,
    val isFoodNoteInEditMode: Boolean = false
)

@HiltViewModel
class PetProfileViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val medicationRepository: MedicationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun toggleHealthNoteEditMode() {
        _uiState.update { state ->
            state.copy(isHealthNoteInEditMode = !state.isHealthNoteInEditMode)
        }
    }

    fun toggleMedicationModal() {
        _uiState.update { state ->
            state.copy(showMedicationModal = !state.showMedicationModal)
        }
    }

    fun onMedicationNameChange(value: String) {
        _uiState.update { state ->
            state.copy(medicationName = value)
        }
    }

    fun onMedicationDosageChange(value: String) {
        _uiState.update { state ->
            state.copy(medicationDosage = value)
        }
    }

    fun onMedicationFrequencyChange(value: String) {
        _uiState.update { state ->
            state.copy(medicationFrequency = value)
        }
    }

    fun toggleRegularMedication(isRegular: Boolean) {
        _uiState.update { state ->
            state.copy(isMedicationRegular = isRegular)
        }
    }

    fun onMedicationStartDateChange(value: Long?) {
        _uiState.update { state ->
            val startDate = value?.let {
                SimpleDateFormat(
                    "dd MMMM yyyy",
                    Locale.getDefault()
                ).format(it)
            }
            state.copy(
                medicationStartDate = value,
                medicationStartDateValue = startDate
            )
        }
    }

    fun onMedicationEndDateChange(value: Long?) {
        _uiState.update { state ->
            val endDate = value?.let {
                SimpleDateFormat(
                    "dd MMMM yyyy",
                    Locale.getDefault()
                ).format(it)
            }
            state.copy(
                medicationEndDate = value,
                medicationEndDateValue = endDate
            )
        }
    }

    fun onMedicationNoteChange(value: String) {
        _uiState.update { state ->
            state.copy(medicationNote = value)
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

    fun onHealthNoteChange(value: String) {
        _uiState.update { state ->
            state.copy(updatedHealthNote = value)
        }
    }

    fun onSaveHealthNoteClick() {
        viewModelScope.launch {
            val pet = uiState.value.pet
            petRepository.updatePet(pet.copy(healthNote = uiState.value.updatedHealthNote))
            toggleHealthNoteEditMode()
            getPetData(pet.id)
        }
    }

    fun onSaveMedicationClick() {
        viewModelScope.launch {
            val petId = uiState.value.pet.id
            val (startDate, endDate) = when (uiState.value.isMedicationRegular) {
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

            val medication = Medication(
                petId = petId,
                name = uiState.value.medicationName,
                dosage = uiState.value.medicationDosage,
                frequency = uiState.value.medicationFrequency,
                startDate = startDate,
                endDate = endDate,
                note = uiState.value.medicationNote
            )

            medicationRepository.addMedication(medication)

            toggleMedicationModal()
            getPetData(petId)
        }
    }

    fun onDeleteMedicationClick(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.deleteMedication(medication)

            val petId = uiState.value.pet.id
            getPetData(petId)
        }
    }

    fun onEditMedicationClick(medication: Medication) {

    }

    fun getPetData(petId: String) {
        viewModelScope.launch {
            val pet = petRepository.getPetById(petId)

            pet?.let { pet ->
                val dob = getPetDob(pet)
                val age = getPetAge(pet, context)

                _uiState.update { state ->
                    state.copy(
                        pet = pet,
                        dob = dob,
                        age = age,
                        updatedHealthNote = pet.healthNote ?: "",
                        medications = medicationRepository.getMedications(petId)
                    )
                }
            }
        }
    }

    fun getPetDob(pet: Pet): String {
        val dobMillis = pet.dobMillis

        val pattern = when (pet.dobPrecision) {
            DobPrecision.EXACT -> "dd MMMM yyyy"
            DobPrecision.YEAR_MONTH -> "MMMM yyyy"
            DobPrecision.YEAR -> "yyyy"
        }

       return SimpleDateFormat(pattern, Locale.getDefault()).format(dobMillis)
    }

    fun getPetAge(pet: Pet, context: Context): String {
        val dobMillis = pet.dobMillis

        val today = LocalDate.now()
        val petBirthDate = Instant.ofEpochMilli(dobMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val period = Period.between(petBirthDate, today)
        val years = period.years
        val months = period.months
        val days = period.days

        return when (pet.dobPrecision) {
            DobPrecision.EXACT -> {
                when {
                    years >= 1 -> context.resources.getQuantityString(R.plurals.years_old_plural, years, years)
                    months >= 1 -> context.resources.getQuantityString(R.plurals.months_old_plural, months, months)
                    days >= 0 -> context.resources.getQuantityString(R.plurals.days_old_plural, days, days)
                    else -> context.getString(R.string.just_born)
                }
            }
            DobPrecision.YEAR_MONTH -> {
                when {
                    years >= 1 -> context.resources.getQuantityString(R.plurals.years_old_plural, years, years)
                    else -> context.resources.getQuantityString(R.plurals.months_old_plural, months, months)
                }
            }
            DobPrecision.YEAR -> {
                when {
                    years >= 1 -> context.resources.getQuantityString(R.plurals.years_old_plural, years, years)
                    else -> context.getString(R.string.less_than_a_year_old)
                }
            }
        }
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            petRepository.deletePet(petId)
        }
    }
}