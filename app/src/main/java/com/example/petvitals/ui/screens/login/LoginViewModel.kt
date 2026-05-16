package com.example.petvitals.ui.screens.login

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.AccountError
import com.example.petvitals.ui.components.SnackbarState
import com.example.petvitals.ui.components.SnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountService: AccountService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventChannel = Channel<LoginEvent>()
    val events = _eventChannel.receiveAsFlow()

    fun onAction(
        action: LoginAction,
        onSuccess: () -> Unit = {}
    ) {
        when (action) {
            is LoginAction.OnEmailChanged -> onEmailChanged(action.email)
            is LoginAction.OnPasswordChanged -> onPasswordChanged(action.password)
            LoginAction.Authenticate -> authenticate(onSuccess)
            LoginAction.SendVerificationEmail -> sendVerificationEmail()
        }
    }

    private fun onPasswordChanged(password: String) {
        _uiState.update { state ->
            state.copy(password = password)
        }
    }

    private fun onEmailChanged(email: String) {
        _uiState.update { state ->
            state.copy(email = email)
        }
    }

    private fun authenticate(onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (
                val result = accountService.signIn(
                    email = uiState.value.email,
                    password = uiState.value.password
                )
            ) {
                is AppResult.Success -> {
                    if (accountService.isEmailVerified) {
                        onSuccess()
                    } else {
                        _eventChannel.send(
                            LoginEvent.OnError(
                                SnackbarState(
                                    message = context.getString(R.string.email_not_verified_error),
                                    actionLabel = context.getString(R.string.resend),
                                    snackbarType = SnackbarType.ERROR,
                                    duration = SnackbarDuration.Indefinite,
                                    onAction = ::sendVerificationEmail
                                )
                            )
                        )
                    }
                }
                is AppResult.Failure -> {
                    _eventChannel.send(
                        LoginEvent.OnError(
                            snackbarState = SnackbarState(
                                message = result.error.toLoginErrorMessage(),
                                snackbarType = SnackbarType.ERROR,
                            )
                        )
                    )
                }
            }
        }
    }

    private fun sendVerificationEmail() {
        viewModelScope.launch {
            when (accountService.sendVerificationEmail()) {
                is AppResult.Success -> Unit
                is AppResult.Failure -> {
                    _eventChannel.send(
                        LoginEvent.OnError(
                            snackbarState = SnackbarState(
                                message = context.getString(R.string.login_failed_to_send_email),
                                snackbarType = SnackbarType.ERROR,
                            )
                        )
                    )
                }
            }
        }
    }

    private fun AccountError.toLoginErrorMessage(): String = when (this) {
        AccountError.EmptyFields -> context.getString(R.string.empty_fields_error)
        AccountError.InvalidCredentials -> context.getString(R.string.invalid_credentials_error)
        AccountError.UserNotFound -> context.getString(R.string.user_not_found_error)
        AccountError.Network -> context.getString(R.string.network_error)
        else -> context.getString(R.string.unexpected_error)
    }
}
