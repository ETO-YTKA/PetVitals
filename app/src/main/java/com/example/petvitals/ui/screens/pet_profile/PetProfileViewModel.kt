package com.example.petvitals.ui.screens.pet_profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.repository.medication.Medication
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
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class PetProfileUiState(
    val pet: Pet = Pet(),
    val dob: String = "",
    val age: String = "",
    val medications: List<Medication> = emptyList(),
    val updatedHealthNote: String = "",
    val foodNote: String = "",

    val isHealthNoteInEditMode: Boolean = false,
    val isFoodNoteInEditMode: Boolean = false
)

@HiltViewModel
class PetProfileViewModel @Inject constructor(
    private val petRepository: PetRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun toggleHealthNoteEditMode() {
        _uiState.update { state ->
            state.copy(isHealthNoteInEditMode = !state.isHealthNoteInEditMode)
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
            petRepository.updatePet(pet.copy(healthNotes = uiState.value.updatedHealthNote))
            toggleHealthNoteEditMode()
            getPetData(pet.id)
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
                        updatedHealthNote = pet.healthNotes ?: "",
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

        val petDob = Calendar.getInstance().apply { timeInMillis = pet.dobMillis }
        val currentTime = Calendar.getInstance()

        val currentYear = currentTime.get(Calendar.YEAR)
        val currentMonth = currentTime.get(Calendar.MONTH)
        val currentDay = currentTime.get(Calendar.DAY_OF_MONTH)

        val petYear = petDob.get(Calendar.YEAR)
        val petMonth = petDob.get(Calendar.MONTH)
        val petDay = petDob.get(Calendar.DAY_OF_MONTH)

        return when {
            petYear == currentYear -> {
                when (pet.dobPrecision) {
                    DobPrecision.EXACT -> {
                        if (petMonth == currentMonth) {
                            val days = currentDay - petDay
                            context.resources.getQuantityString(
                                R.plurals.days_old_plural,
                                days,
                                days
                            )
                        } else {
                            val months = currentMonth - petMonth
                            context.resources.getQuantityString(
                                R.plurals.months_old_plural,
                                months,
                                months
                            )
                        }
                    }
                    DobPrecision.YEAR_MONTH -> {
                        val months = currentMonth - petMonth
                        context.resources.getQuantityString(
                            R.plurals.months_old_plural,
                            months,
                            months
                        )
                    }
                    DobPrecision.YEAR -> context.resources.getQuantityString(
                        R.plurals.years_old_plural,
                        0,
                        0
                    )
                }
            }
            else -> {
                val years = currentYear - petYear
                context.resources.getQuantityString(R.plurals.years_old_plural, years, years)
            }
        }
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            petRepository.deletePet(petId)
        }
    }
}