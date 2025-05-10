package com.example.petvitals.ui.screens.pet_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.data.repository.pet.PetRepository
import com.example.petvitals.data.service.account.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PetProfileUiState(
    val name: String = "",
    val species: String = "",
    val birthDate: String = ""
)

@HiltViewModel
class PetProfileViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val accountService: AccountService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun getPetData(petId: String) {
        viewModelScope.launch {
            val pet = petRepository.getPetById(
                userId = accountService.currentUserId,
                petId = petId
            )

            pet?.let {
                val birthDay = it.birthDate.getOrDefault("day", "").toString()
                val birthMonth = it.birthDate.getOrDefault("month", "").toString()
                val birthYear = it.birthDate["year"].toString()

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
                        name = it.name,
                        species = it.species,
                        birthDate = birthDate
                    )
                }
            }
        }
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            petRepository.deletePet(
                userId = accountService.currentUserId,
                petId = petId
            )
        }
    }
}