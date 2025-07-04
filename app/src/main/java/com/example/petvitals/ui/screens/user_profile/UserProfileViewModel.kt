package com.example.petvitals.ui.screens.user_profile

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.petvitals.data.repository.pet.PetRepository
import com.example.petvitals.data.repository.user.User
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
    val username: String = "",
    val email: String = "",

    //Modal
    val showDeleteAccountModal: Boolean = false,
    val password: String = "",
    val isPasswordIncorrect: Boolean = false
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository,
    private val petRepository: PetRepository,
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
            try {
                val password = uiState.value.password
                val email = accountService.currentUserEmail ?: ""

                accountService.signIn(email, password)
            } catch (e: Exception) {
                Log.d("UserProfileViewModel", "deleteAccount: $e")
                _uiState.update { state -> state.copy(isPasswordIncorrect = true) }
                return@launchCatching
            }

            userRepository.deleteCurrentUser()
            //petRepository.deleteAllUserPetsPets()

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
            val user = userRepository.getCurrentUser() ?: User()

            _uiState.update { state ->
                state.copy(
                    username = user.username,
                    email = user.email
                )
            }
        }
    }

    fun sendPasswordResetEmail() {
        launchCatching {
            val email = accountService.currentUserEmail ?: ""
            accountService.sendPasswordResetEmail(email)
        }
    }
}