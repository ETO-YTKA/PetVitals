package com.example.petvitals.ui.screens.log_in

import androidx.lifecycle.viewModelScope
import com.example.petvitals.SignUp
import com.example.petvitals.Splash
import com.example.petvitals.model.service.AccountService
import com.example.petvitals.ui.screens.PetVitalsAppViewModel
import com.google.firebase.FirebaseNetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogInUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false
)

@HiltViewModel
class LogInViewModel @Inject constructor(
    private val accountService: AccountService
) : PetVitalsAppViewModel() {

    private val _uiState = MutableStateFlow(LogInUiState())
    val uiState = _uiState.asStateFlow()

    fun onPasswordChange(password: String) {
        _uiState.update { state ->
            state.copy(password = password)
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { state ->
            state.copy(email = email)
        }
    }

    fun onLogInClick(navigateTo: (Any) -> Unit) {
        viewModelScope.launch {
            try {
                accountService.signIn(
                    email = uiState.value.email,
                    password = uiState.value.password
                )
                navigateTo(Splash)
            } catch (e: Exception) {
                when(e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        _uiState.update { state ->
                            state.copy(errorMessage = "Invalid email or password.")
                        }
                    }
                    is FirebaseAuthInvalidUserException -> {
                        _uiState.update { state ->
                            state.copy(errorMessage = "User not found. Please check your email.")
                        }
                    }
                    is FirebaseNetworkException -> {
                        _uiState.update { state ->
                            state.copy(errorMessage = "A network error occurred. Please check your connection.")
                        }
                    }
                    else -> {
                        _uiState.update { state ->
                            state.copy(errorMessage = "An unexpected error occurred.")
                        }
                    }
                }
            }
        }
    }

    fun onSignUpClick(navigateTo: (Any) -> Unit) {
        navigateTo(SignUp)
    }
}