package com.example.petvitals.ui.screens.log_in

import com.example.petvitals.Hallo
import com.example.petvitals.SignUp
import com.example.petvitals.model.service.AccountService
import com.example.petvitals.ui.screens.PetVitalsAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class LogInUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null
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
        launchCatching {
            _uiState.update { state ->
                state.copy(errorMessage = null)
            }
            accountService.signIn(email = uiState.value.email, password = uiState.value.password)
            navigateTo(Hallo)
        }
    }

    fun onSignUpClick(navigateTo: (Any) -> Unit) {
        navigateTo(SignUp)
    }
}