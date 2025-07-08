package com.example.petvitals.ui.screens.user_profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.repository.user.User
import com.example.petvitals.data.repository.user.UserRepository
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.ui.screens.PetVitalsAppViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val passwordErrorMessage: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
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
        viewModelScope.launch {
            try {
                val password = uiState.value.password
                val email = accountService.currentUserEmail ?: ""

                accountService.signIn(email, password)

                userRepository.deleteCurrentUser()
                accountService.deleteAccount()
            } catch (e: Exception) {
                Log.d("UserProfileViewModel", "deleteAccount: $e")

                val errorMessage = when(e) {
                    is FirebaseAuthInvalidCredentialsException -> context.getString(R.string.incorrect_password_error)
                    is FirebaseNetworkException -> context.getString(R.string.network_error)
                    else -> context.getString(R.string.unexpected_error)
                }
                _uiState.update { state -> state.copy(passwordErrorMessage = errorMessage) }
            }
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