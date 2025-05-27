package com.example.petvitals.ui.screens.pet_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.data.repository.pet.DobPrecision
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class PetProfileUiState(
    val pet: Pet = Pet(),
    val dob: String = "",
)

@HiltViewModel
class PetProfileViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun getPetData(petId: String) {
        viewModelScope.launch {
            val pet = petRepository.getPetById(petId)

            pet?.let { pet ->
                val dobMillis = pet.dobMillis
                val pattern = when (pet.dobPrecision) {
                    DobPrecision.EXACT -> "dd MMMM yyyy"
                    DobPrecision.YEAR_MONTH -> "MMMM yyyy"
                    DobPrecision.YEAR -> "yyyy"
                }
                val dobString = SimpleDateFormat(pattern, Locale.getDefault()).format(dobMillis)

                _uiState.update { state ->
                    state.copy(
                        pet = pet,
                        dob = dobString
                    )
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