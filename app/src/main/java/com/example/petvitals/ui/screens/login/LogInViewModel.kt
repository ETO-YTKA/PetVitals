package com.example.petvitals.ui.screens.login

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.ui.components.SnackbarState
import com.example.petvitals.ui.components.SnackbarType
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
import timber.log.Timber
import javax.inject.Inject

data class LogInUiState(
    val email: String = "",
    val password: String = "",
    val snackbarState: SnackbarState? = null,
    val passwordErrorMessage: String? = null,
    val emailErrorMessage: String? = null,
)

@HiltViewModel
class LogInViewModel @Inject constructor(
    private val accountService: AccountService,
    @ApplicationContext private val context: Context
) : PetVitalsAppViewModel() {

    private val _uiState = MutableStateFlow(LogInUiState())
    val uiState = _uiState.asStateFlow()

    fun updatePassword(password: String) {
        _uiState.update { state ->
            state.copy(password = password)
        }
    }

    fun updateEmail(email: String) {
        _uiState.update { state ->
            state.copy(email = email)
        }
    }

    fun authenticate(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                accountService.signIn(
                    email = uiState.value.email,
                    password = uiState.value.password
                )

                if (accountService.isEmailVerified) {
                    onSuccess()
                } else {
                    _uiState.update { state ->
                        state.copy(
                            snackbarState = SnackbarState(
                                message = context.getString(R.string.email_not_verified_error),
                                actionLabel = context.getString(R.string.resend),
                                snackbarType = SnackbarType.ERROR,
                                duration = SnackbarDuration.Indefinite,
                                onAction = ::resendVerificationEmail
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.d("$e: ${e.message.orEmpty()}")

                val errorMessage = when(e) {
                    is IllegalArgumentException -> {
                        context.getString(R.string.empty_fields_error)
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        context.getString(R.string.invalid_credentials_error)
                    }
                    is FirebaseAuthInvalidUserException -> {
                        context.getString(R.string.user_not_found_error)
                    }
                    is FirebaseNetworkException -> {
                        context.getString(R.string.network_error)
                    }
                    else -> {
                        context.getString(R.string.unexpected_error)
                    }
                }

                _uiState.update { state ->
                    state.copy(
                        snackbarState = SnackbarState(
                            message = errorMessage,
                            snackbarType = SnackbarType.ERROR,
                        )
                    )
                }
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            accountService.sendVerificationEmail()
        }
    }
}
