package com.example.petvitals.ui.screens.pets

import androidx.lifecycle.viewModelScope
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetRepository
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.ui.screens.PetVitalsAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PetsUiState(
    val displayName: String = "",
    val pets: List<Pet> = listOf(),
    val isRefreshing: Boolean = false
)

@HiltViewModel
class PetsViewModel @Inject constructor(
    private val accountService: AccountService,
    private val petRepository: PetRepository
) : PetVitalsAppViewModel() {

    private val _uiState = MutableStateFlow(PetsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshPets()
    }

    fun initialize(onNavigateToSplash: () -> Unit) {
        launchCatching {
            accountService.currentUser.collect { user ->
                if (user == null) onNavigateToSplash()
            }
        }
    }

    fun refreshPets() {
        _uiState.update { state -> state.copy(isRefreshing = true) }
        viewModelScope.launch {
            val pets = petRepository.getUserPets()
            _uiState.update { state -> state.copy(pets = pets, isRefreshing = false) }
        }
    }
}