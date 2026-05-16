package com.example.petvitals.ui.screens.passwordreset

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.AccountError
import com.example.petvitals.domain.error.EmailErrors
import com.example.petvitals.domain.validator.UserDataValidator
import com.example.petvitals.ui.components.SnackbarState
import com.example.petvitals.ui.components.SnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    private val accountService: AccountService,
    @ApplicationContext private val context: Context,
    private val dataValidator: UserDataValidator
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordResetUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventChannel = Channel<PasswordResetEvent>()
    val events = _eventChannel.receiveAsFlow()

    fun onAction(action: PasswordResetAction) {
        when (action) {
            is PasswordResetAction.OnEmailChange -> onEmailChange(action.email)
            is PasswordResetAction.OnSendPasswordResetEmail -> onSendPasswordResetEmail()
        }
    }

    private fun onEmailChange(email: String) {
        _uiState.update { state ->
            state.copy(
                email = email,
                errorMessage = null,
                isPasswordResetEmailSent = false
            )
        }
    }

    private fun onSendPasswordResetEmail() {
        viewModelScope.launch {
            val email = uiState.value.email

            when (val result = dataValidator.validateEmail(email)) {

                is AppResult.Failure -> {
                    val message = when(result.error) {
                        EmailErrors.EMPTY_FIELD -> context.getString(R.string.empty_field_error)
                        EmailErrors.INVALID_EMAIL -> context.getString(R.string.invalid_email_error)
                    }

                    _uiState.update { state -> state.copy(errorMessage = message) }
                }
                is AppResult.Success -> {
                    _uiState.update { state -> state.copy(errorMessage = null, isLoading = true) }

                    when (val resetResult = accountService.sendPasswordResetEmail(email)) {
                        is AppResult.Success -> {
                            _uiState.update { state ->
                                state.copy(
                                    isLoading = false,
                                    isPasswordResetEmailSent = true
                                )
                            }
                        }
                        is AppResult.Failure -> {
                            _uiState.update { state -> state.copy(isLoading = false) }
                            _eventChannel.send(
                                PasswordResetEvent.OnShowSnackbar(
                                    snackbarState = SnackbarState(
                                        message = resetResult.error.toResetPasswordErrorMessage(),
                                        snackbarType = SnackbarType.ERROR,
                                        duration = SnackbarDuration.Long
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun AccountError.toResetPasswordErrorMessage(): String = when (this) {
        AccountError.Network -> context.getString(R.string.network_error)
        else -> context.getString(R.string.failed_to_send_password_reset_email)
    }
}
