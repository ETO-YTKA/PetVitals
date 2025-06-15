package com.example.petvitals.ui.screens.pet_profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.repository.food.Food
import com.example.petvitals.data.repository.food.FoodRepository
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
    val food: List<Food> = emptyList(),
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

    val foodName: String = "",
    val foodPortion: String = "",
    val foodFrequency: String = "",
    val foodNote: String = "",

    val showAddMedicationModal: Boolean = false,
    val showAddFoodModal: Boolean = false,
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    val showOnDeleteModal: Boolean = false,

    val isHealthNoteInEditMode: Boolean = false,
    val isFoodNoteInEditMode: Boolean = false,

    val medicationNameErrorMessage: String? = null,
    val medicationDosageErrorMessage: String? = null,
    val medicationFrequencyErrorMessage: String? = null,
    val medicationStartDateErrorMessage: String? = null,
    val medicationEndDateErrorMessage: String? = null,
    val medicationNoteErrorMessage: String? = null,

    val foodNameErrorMessage: String? = null,
    val foodPortionErrorMessage: String? = null,
    val foodFrequencyErrorMessage: String? = null,
    val foodNoteErrorMessage: String? = null,

    val isMedicationNameError: Boolean = false,
    val isMedicationDosageError: Boolean = false,
    val isMedicationFrequencyError: Boolean = false,
    val isMedicationStartDateError: Boolean = false,
    val isMedicationEndDateError: Boolean = false,
    val isMedicationNoteError: Boolean = false,

    val isFoodNameError: Boolean = false,
    val isFoodPortionError: Boolean = false,
    val isFoodFrequencyError: Boolean = false,
    val isFoodNoteError: Boolean = false,
)

@HiltViewModel
class PetProfileViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val medicationRepository: MedicationRepository,
    private val foodRepository: FoodRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetProfileUiState())
    val uiState = _uiState.asStateFlow()


    fun toggleOnDeleteModal() {
        _uiState.update { state ->
            state.copy(showOnDeleteModal = !state.showOnDeleteModal)
        }
    }
    fun toggleHealthNoteEditMode() {
        _uiState.update { state ->
            state.copy(isHealthNoteInEditMode = !state.isHealthNoteInEditMode)
        }
    }

    fun toggleFoodNoteEditMode() {
        _uiState.update { state ->
            state.copy(isFoodNoteInEditMode = !state.isFoodNoteInEditMode)
        }
    }

    fun toggleMedicationModal() {
        _uiState.update { state ->
            state.copy(showAddMedicationModal = !state.showAddMedicationModal)
        }
    }

    fun toggleFoodModal() {
        _uiState.update { state ->
            state.copy(showAddFoodModal = !state.showAddFoodModal)
        }
    }

    fun toggleRegularMedication(isRegular: Boolean) {
        _uiState.update { state ->
            state.copy(isMedicationRegular = isRegular)
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

    fun onFoodNameChange(value: String) {
        _uiState.update { state ->
            state.copy(foodName = value)
        }
    }

    fun onFoodPortionChange(value: String) {
        _uiState.update { state ->
            state.copy(foodPortion = value)
        }
    }

    fun onFoodFrequencyChange(value: String) {
        _uiState.update { state ->
            state.copy(foodFrequency = value)
        }
    }

    fun onFoodNoteChange(value: String) {
        _uiState.update { state ->
            state.copy(foodNote = value)
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

    fun onUpdatedFoodNoteChange(value: String) {
        _uiState.update { state ->
            state.copy(updatedFoodNote = value)
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

    private fun validateMedicationForm(): Boolean {
        val currentState = _uiState.value
        var isValid = true

        _uiState.update { it.copy(
            isMedicationNameError = false,
            isMedicationDosageError = false,
            isMedicationFrequencyError = false,
            isMedicationStartDateError = false,
            isMedicationEndDateError = false,
            isMedicationNoteError = false,
            medicationNameErrorMessage = null,
            medicationFrequencyErrorMessage = null,
            medicationDosageErrorMessage = null,
            medicationStartDateErrorMessage = null,
            medicationEndDateErrorMessage = null,
            medicationNoteErrorMessage = null
        )}

        if (currentState.medicationName.isBlank()) {
            _uiState.update { it.copy(
                isMedicationNameError = true,
                medicationNameErrorMessage = context.getString(R.string.name_cannot_be_empty_error)
            ) }
            isValid = false
        } else if (currentState.medicationName.length > 50) {
            _uiState.update { it.copy(
                isMedicationNameError = true,
                medicationNameErrorMessage = context.getString(R.string.name_cannot_be_longer_than_error)
            ) }
            isValid = false
        }

        if (currentState.medicationDosage.isBlank()) {
            _uiState.update { it.copy(
                isMedicationDosageError = true,
                medicationDosageErrorMessage = context.getString(R.string.dosage_cannot_be_empty_error)
            ) }
            isValid = false
        } else if (currentState.medicationDosage.length > 50) {
            _uiState.update { it.copy(
                isMedicationDosageError = true,
                medicationDosageErrorMessage = context.getString(R.string.dosage_cannot_be_longer_than_error)
            ) }
            isValid = false
        }

        if (currentState.medicationFrequency.isBlank()) {
            _uiState.update { it.copy(
                isMedicationFrequencyError = true,
                medicationFrequencyErrorMessage = context.getString(R.string.frequency_cannot_be_empty_error)
            ) }
            isValid = false
        } else if (currentState.medicationFrequency.length > 50) {
            _uiState.update { it.copy(
                isMedicationFrequencyError = true,
                medicationFrequencyErrorMessage = context.getString(R.string.frequency_cannot_be_longer_than_error)
            ) }
            isValid = false
        }

        if (currentState.medicationNote.length > 500) {
            _uiState.update { it.copy(
                isMedicationNoteError = true,
                medicationNoteErrorMessage = context.getString(R.string.note_cannot_be_longer_than_error)
            ) }
            isValid = false
        }

        if (!currentState.isMedicationRegular) {
            val startDate = currentState.medicationStartDate
            val endDate = currentState.medicationEndDate

            if (startDate == null) {
                _uiState.update { it.copy(
                    isMedicationStartDateError = true,
                    medicationStartDateErrorMessage = context.getString(
                        R.string.start_date_must_be_selected_error)
                ) }
                isValid = false
            }

            if (startDate != null && endDate != null && endDate < startDate) {
                _uiState.update { it.copy(
                    isMedicationEndDateError = true,
                    medicationEndDateErrorMessage = context.getString(
                        R.string.end_date_must_be_after_start_date_error
                    )
                ) }
                isValid = false
            }
        }

        return isValid
    }

    fun onSaveMedicationClick() {
        if (validateMedicationForm()) {
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
    }

    fun onDeleteMedicationClick(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.deleteMedication(medication)

            val petId = uiState.value.pet.id
            getPetData(petId)
        }
    }

    fun onEditMedicationClick(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.updateMedication(medication)
            getPetData(medication.petId)
        }
    }

    fun onSaveFoodNoteClick() {
        viewModelScope.launch {
            val pet = uiState.value.pet
            petRepository.updatePet(pet.copy(foodNote = uiState.value.updatedFoodNote))
            toggleFoodNoteEditMode()
            getPetData(pet.id)
        }
    }

    private fun validateFoodForm(): Boolean {
        val currentState = _uiState.value
        var isValid = true

        _uiState.update { it.copy(
            isFoodNameError = false,
            isFoodPortionError = false,
            isFoodFrequencyError = false,
            isFoodNoteError = false,
            foodNameErrorMessage = null,
            foodPortionErrorMessage = null,
            foodFrequencyErrorMessage = null,
            foodNoteErrorMessage = null
        )}

        if (currentState.foodName.isBlank()) {
            _uiState.update { it.copy(
                isFoodNameError = true,
                foodNameErrorMessage = context.getString(R.string.name_cannot_be_empty_error)
            ) }
            isValid = false
        } else if (currentState.foodName.length > 50) {
            _uiState.update { it.copy(
                isFoodNameError = true,
                foodNameErrorMessage = context.getString(R.string.name_cannot_be_longer_than_error)
            ) }
            isValid = false
        }

        if (currentState.foodPortion.isBlank()) {
            _uiState.update { it.copy(
                isFoodPortionError = true,
                foodPortionErrorMessage = context.getString(R.string.portion_cannot_be_empty_error)
            ) }
            isValid = false
        } else if (currentState.foodPortion.length > 50) {
            _uiState.update { it.copy(
                isFoodPortionError = true,
                foodPortionErrorMessage = context.getString(R.string.portion_cannot_be_longer_than_error)
            ) }
            isValid = false
        }

        if (currentState.foodFrequency.isBlank()) {
            _uiState.update { it.copy(
                isFoodFrequencyError = true,
                foodFrequencyErrorMessage = context.getString(R.string.frequency_cannot_be_empty_error)
            ) }
            isValid = false
        } else if (currentState.foodFrequency.length > 50) {
            _uiState.update { it.copy(
                isFoodFrequencyError = true,
                foodFrequencyErrorMessage = context.getString(R.string.frequency_cannot_be_longer_than_error)
            ) }
            isValid = false
        }

        if (currentState.foodNote.length > 500) {
            _uiState.update { it.copy(
                isFoodNoteError = true,
                foodNoteErrorMessage = context.getString(R.string.note_cannot_be_longer_than_error)
            ) }
            isValid = false
        }

        return isValid
    }

    fun onSaveFoodClick() {
        if (validateFoodForm()) {
            viewModelScope.launch {
                val petId = uiState.value.pet.id

                val food = Food(
                    petId = petId,
                    name = uiState.value.foodName,
                    portion = uiState.value.foodPortion,
                    frequency = uiState.value.foodFrequency,
                    note = uiState.value.foodNote
                )

                foodRepository.addFood(food)

                toggleFoodModal()
                getPetData(petId)
            }
        }
    }

    fun onDeleteFoodClick(food: Food) {
        viewModelScope.launch {
            foodRepository.deleteFood(food)
            getPetData(food.petId)
        }
    }

    fun onEditFoodClick(food: Food) {
        viewModelScope.launch {
            foodRepository.updateFood(food)
            getPetData(food.petId)
        }
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
                        medications = medicationRepository.getMedications(petId),
                        food = foodRepository.getFood(petId)
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