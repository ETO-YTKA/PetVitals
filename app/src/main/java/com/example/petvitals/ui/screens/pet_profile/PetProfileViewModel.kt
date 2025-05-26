package com.example.petvitals.ui.screens.pet_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PetProfileUiState(
    val pet: Pet = Pet(),
    val birthDate: String = "",
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
                val birthDay = pet.birthDate.getOrDefault("day", "").toString()
                val birthMonth = pet.birthDate.getOrDefault("month", "").toString()
                val birthYear = pet.birthDate["year"].toString()

                val birthDate = when {
                    birthDay.isNotEmpty() && birthMonth.isNotEmpty() -> {
                        "$birthDay.$birthMonth.$birthYear"
                    }
                    birthMonth.isNotEmpty() -> {
                        "$birthMonth.$birthYear"
                    }
                    else -> {
                        birthYear
                    }
                }

                _uiState.update { state ->
                    state.copy(
                        pet = pet,
                        birthDate = birthDate
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