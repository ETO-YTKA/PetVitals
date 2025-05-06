package com.example.petvitals.ui.screens.user_profile

import androidx.lifecycle.viewModelScope
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

data class UserProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val showDeleteAccountModal: Boolean = false
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository,
    private val petRepository: PetRepository
): PetVitalsAppViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getUserData()
    }

    fun logout() {
        launchCatching {
            accountService.logout()
        }
    }

    fun deleteAccount() {
        launchCatching {
            accountService.signIn(uiState.value.email, uiState.value.password)

            userRepository.deleteUser(accountService.currentUserId)
            petRepository.deleteAllPets(accountService.currentUserId)

            accountService.deleteAccount()
        }
    }

    fun showModal(show: Boolean) {
        _uiState.update { state ->
            state.copy(showDeleteAccountModal = show)
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { state -> state.copy(password = password) }
    }

    fun getUserData() {
        viewModelScope.launch {
            val userData = userRepository.getCurrentUserData(accountService.currentUserId)

            _uiState.update { state ->
                state.copy(
                    displayName = userData.getString("displayName") ?: "Anonymous",
                    email = userData.getString("email") ?: "wtf bro where's your email😭😭😭"
                )
            }
        }
    }
}