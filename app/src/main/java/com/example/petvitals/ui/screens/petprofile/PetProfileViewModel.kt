package com.example.petvitals.ui.screens.petprofile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.models.Food
import com.example.petvitals.domain.models.Medication
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.repository.FoodRepository
import com.example.petvitals.domain.repository.MedicationRepository
import com.example.petvitals.domain.repository.PetPermissionRepository
import com.example.petvitals.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DateTimeException
import java.time.LocalDate
import java.time.Month
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
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

    //Modals state
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    val showOnDeleteModal: Boolean = false,
    val showShareModal: Boolean = false,

    //States
    val isHealthNoteInEditMode: Boolean = false,
    val isFoodNoteInEditMode: Boolean = false
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

    fun onDeleteFoodClick(food: Food) {
        viewModelScope.launch {
            foodRepository.deleteFood(food)
            getPetData(food.petId)
        }
    }

    fun getPetData(petId: String) {
        _uiState.update { state ->
            state.copy(isLoading = true)
        }

        viewModelScope.launch {
            val response = petRepository.getPetById(petId)

            when (response) {
                is AppResult.Success -> {
                    val pet = response.data

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
                                food = foodRepository.getAllFood(petId),
                                permissionLevel = petPermissionRepository.getCurrentUserPermissionLevel(petId) ?: PermissionLevel.VIEWER,
                                isLoading = false
                            )
                        }
                    }
                }
                is AppResult.Failure -> return@launch //TODO
            }

        }
    }

    fun getPetDob(pet: Pet): String {
        val year = pet.dobYear ?: return context.getString(R.string.unknown)
        val month = pet.dobMonth
        val day = pet.dobDay

        return runCatching {
            when {
                month == null -> year.toString()
                day == null -> "${Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())} $year"
                else -> LocalDate.of(year, month, day)
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault()))
            }
        }.getOrElse {
            context.getString(R.string.unknown)
        }
    }

    fun getPetAge(pet: Pet, context: Context): String {
        val year = pet.dobYear ?: return context.getString(R.string.unknown)
        val month = pet.dobMonth
        val day = pet.dobDay

        val today = LocalDate.now()
        val petBirthDate = try {
            LocalDate.of(year, month ?: 1, day ?: 1)
        } catch (_: DateTimeException) {
            return context.getString(R.string.unknown)
        }

        if (petBirthDate.isAfter(today)) {
            return context.getString(R.string.unknown)
        }

        val period = Period.between(petBirthDate, today)
        val years = period.years
        val months = period.months
        val days = period.days

        return when {
            day != null -> {
                when {
                    years >= 1 -> context.resources.getQuantityString(R.plurals.years_old_plural, years, years)
                    months >= 1 -> context.resources.getQuantityString(R.plurals.months_old_plural, months, months)
                    days >= 0 -> context.resources.getQuantityString(R.plurals.days_old_plural, days, days)
                    else -> context.getString(R.string.just_born)
                }
            }
            month != null -> {
                when {
                    years >= 1 -> context.resources.getQuantityString(R.plurals.years_old_plural, years, years)
                    else -> context.resources.getQuantityString(R.plurals.months_old_plural, months, months)
                }
            }
            else -> {
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