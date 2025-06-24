package com.example.petvitals.ui.screens.password_reset

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.Result
import com.example.petvitals.domain.SignUpDataValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResetPasswordUiState(
    val email: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    private val accountService: AccountService,
    @ApplicationContext private val context: Context,
    val dataValidator: SignUpDataValidator
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { state -> state.copy(email = email, errorMessage = null) }
    }

    fun onSendPasswordResetEmail() {
        viewModelScope.launch {
            val email = uiState.value.email
            val result = dataValidator.validateEmail(email)

            when (result) {
                is Result.Error -> {
                    when(result.error) {
                        SignUpDataValidator.EmailErrors.EMPTY_FIELD -> _uiState.update { state ->
                            state.copy(errorMessage = context.getString(R.string.empty_field_error))
                        }
                        SignUpDataValidator.EmailErrors.INVALID_EMAIL -> _uiState.update { state ->
                            state.copy(errorMessage = context.getString(R.string.invalid_email_error))
                        }
                    }
                }
                is Result.Success -> {
                    _uiState.update { state -> state.copy(errorMessage = null) }

                    accountService.sendPasswordResetEmail(email)
                }
            }
        }
    }
}