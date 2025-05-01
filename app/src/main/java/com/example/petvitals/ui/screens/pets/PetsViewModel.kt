package com.example.petvitals.ui.screens.pets

import androidx.lifecycle.viewModelScope
import com.example.petvitals.Splash
import com.example.petvitals.data.Pet
import com.example.petvitals.data.repository.pet.PetRepository
import com.example.petvitals.data.repository.user.UserRepository
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
    val pets: List<Pet> = listOf()
)

@HiltViewModel
class PetsViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository,
    private val petRepository: PetRepository
) : PetVitalsAppViewModel() {

    private val _uiState = MutableStateFlow(PetsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getPets()
    }

    fun initialize(restartApp: (Any) -> Unit) {
        launchCatching {
            accountService.currentUser.collect { user ->
                if (user == null) restartApp(Splash)
                _uiState.update { state -> state.copy(displayName = userRepository.getUserDisplayName(user!!.id)) }
            }
        }
    }

    fun getPets() {
        viewModelScope.launch {
            val pets = petRepository.getUserPets(userId = accountService.currentUserId)
            _uiState.update { state -> state.copy(pets = pets) }
        }
    }
}