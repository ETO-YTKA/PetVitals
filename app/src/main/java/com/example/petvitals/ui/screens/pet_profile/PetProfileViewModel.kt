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
import com.example.petvitals.data.repository.pet_permission.PermissionLevel
import com.example.petvitals.data.repository.pet_permission.PetPermissionRepository
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
import java.util.Locale
import javax.inject.Inject

data class PetProfileUiState(
    val isLoading: Boolean = false,

    //Main screen
    val pet: Pet = Pet(),
    val dob: String = "",
    val age: String = "",
    val medications: List<Medication> = emptyList(),
    val food: List<Food> = emptyList(),
    val updatedHealthNote: String = "",
    val updatedFoodNote: String = "",
    val permissionLevel: PermissionLevel = PermissionLevel.VIEWER,

    //Food modal
    val foodId: String? = null,
    val foodName: String = "",
    val foodPortion: String = "",
    val foodFrequency: String = "",
    val foodNote: String = "",

    //Modals state
    val showAddFoodModal: Boolean = false,
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    val showOnDeleteModal: Boolean = false,
    val showShareModal: Boolean = false,

    //States
    val isHealthNoteInEditMode: Boolean = false,
    val isFoodNoteInEditMode: Boolean = false,

    //Food error messages
    val foodNameErrorMessage: String? = null,
    val foodPortionErrorMessage: String? = null,
    val foodFrequencyErrorMessage: String? = null,
    val foodNoteErrorMessage: String? = null,

    //Food error states
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
    private val petPermissionRepository: PetPermissionRepository,
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

    fun toggleFoodModal() {
        _uiState.update { state ->
            state.copy(
                showAddFoodModal = !state.showAddFoodModal,
                foodId = null,
                foodName = "",
                foodPortion = "",
                foodFrequency = "",
                foodNote = ""
            )
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
            petRepository.savePet(pet.copy(healthNote = uiState.value.updatedHealthNote))
            toggleHealthNoteEditMode()
            getPetData(pet.id)
        }
    }

    fun onDeleteMedicationClick(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.deleteMedication(medication)

            val petId = uiState.value.pet.id
            getPetData(petId)
        }
    }

    fun onSaveFoodNoteClick() {
        viewModelScope.launch {
            val pet = uiState.value.pet
            petRepository.savePet(pet.copy(foodNote = uiState.value.updatedFoodNote))
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
                val food: Food
                val petId = uiState.value.pet.id

                if (uiState.value.foodId == null) {
                    food = Food(
                        petId = petId,
                        name = uiState.value.foodName,
                        portion = uiState.value.foodPortion,
                        frequency = uiState.value.foodFrequency,
                        note = uiState.value.foodNote
                    )
                } else {
                    food = Food(
                        id = uiState.value.foodId!!,
                        petId = petId,
                        name = uiState.value.foodName,
                        portion = uiState.value.foodPortion,
                        frequency = uiState.value.foodFrequency,
                        note = uiState.value.foodNote
                    )
                }

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
        _uiState.update {
            it.copy(
                foodId = food.id,
                foodName = food.name,
                foodPortion = food.portion,
                foodFrequency = food.frequency,
                foodNote = food.note,
                showAddFoodModal = true
            )
        }
    }

    fun getPetData(petId: String) {
        _uiState.update { state ->
            state.copy(isLoading = true)
        }

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
                        food = foodRepository.getFood(petId),
                        permissionLevel = petPermissionRepository.getCurrentUserPermissionLevel(petId) ?: PermissionLevel.VIEWER,
                        isLoading = false
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