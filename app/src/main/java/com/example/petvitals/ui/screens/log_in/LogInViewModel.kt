package com.example.petvitals.ui.screens.log_in

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.SignUp
import com.example.petvitals.Splash
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.ui.screens.PetVitalsAppViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val accountService: AccountService,
    @ApplicationContext private val context: Context
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
                Log.d("LogInViewModel", e.message.orEmpty())
                when(e) {
                    is IllegalArgumentException -> _uiState.update { state ->
                        state.copy(errorMessage = context.getString(R.string.empty_fields_error))
                    }
                    is FirebaseAuthInvalidCredentialsException -> _uiState.update { state ->
                        state.copy(errorMessage = context.getString(R.string.invalid_email_password_error))
                    }
                    is FirebaseAuthInvalidUserException -> _uiState.update { state ->
                        state.copy(errorMessage = context.getString(R.string.user_not_found_error))
                    }
                    is FirebaseNetworkException -> _uiState.update { state ->
                        state.copy(errorMessage = context.getString(R.string.network_error))
                    }
                    else -> _uiState.update { state ->
                        state.copy(errorMessage = context.getString(R.string.unexpected_error))
                    }
                }
            }
        }
    }

    fun onSignUpClick(navigateTo: (Any) -> Unit) {
        navigateTo(SignUp)
    }
}