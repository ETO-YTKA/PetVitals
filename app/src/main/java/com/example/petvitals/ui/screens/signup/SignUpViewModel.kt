package com.example.petvitals.ui.screens.signup

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.DisplayNameError
import com.example.petvitals.domain.error.EmailErrors
import com.example.petvitals.domain.error.PasswordError
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.repository.UserRepository
import com.example.petvitals.domain.validator.UserDataValidator
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
import javax.inject.Inject

data class SignUpUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordHidden: Boolean = true,
    val snackbarState: SnackbarState? = null,

    val displayNameErrorMessage: String? = null,
    val emailErrorMessage: String? = null,
    val passwordErrorMessage: String? = null,
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    val accountService: AccountService,
    val userRepository: UserRepository,
    val userDataValidator: UserDataValidator,
    @ApplicationContext private val context: Context
) : PetVitalsAppViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    fun updateDisplayName(name: String) {
        _uiState.update { state ->
            state.copy(displayName = name)
        }
        validateDisplayName(name)
    }

    private fun validateDisplayName(displayName: String){

        when(val result = userDataValidator.validateDisplayName(displayName)) {
            is AppResult.Success -> {
                _uiState.update { state ->
                    state.copy(
                        displayNameErrorMessage = null
                    )
                }
            }
            is AppResult.Failure -> {
                val message = when(result.error) {

                    DisplayNameError.EMPTY_FIELD -> {
                        context.getString(R.string.empty_field_error)
                    }
                    DisplayNameError.TOO_LONG -> {
                        context.getString(R.string.display_name_too_long_error)
                    }
                    DisplayNameError.INVALID_CHARACTERS -> {
                        context.getString(R.string.invalid_characters_error)
                    }
                }
                _uiState.update { state -> state.copy(displayNameErrorMessage = message) }
            }
        }
    }

    fun updatePassword(password: String) {
        _uiState.update { state ->
            state.copy(password = password)
        }
        validatePassword(password)
    }

    private fun validatePassword(password: String) {

        when(val result = userDataValidator.validatePassword(password)) {
            is AppResult.Success -> {
                _uiState.update { state ->
                    state.copy(
                        passwordErrorMessage = null
                    )
                }
            }
            is AppResult.Failure -> {
                val message = when(result.error) {

                    PasswordError.EMPTY_FIELD -> {
                        context.getString(R.string.empty_field_error)
                    }
                    PasswordError.HAS_WHITESPACE -> {
                        context.getString(R.string.password_whitespace_error)
                    }
                    PasswordError.TOO_SHORT -> {
                        context.getString(R.string.password_short_error)
                    }
                    PasswordError.NO_DIGIT -> {
                        context.getString(R.string.password_no_digit_error)
                    }
                    PasswordError.NO_UPPERCASE -> {
                        context.getString(R.string.password_no_uppercase_error)
                    }
                    PasswordError.NO_LOWERCASE -> {
                        context.getString(R.string.password_no_lowercase_error)
                    }
                }
                _uiState.update { state -> state.copy(passwordErrorMessage = message) }
            }
        }
    }

    fun updateEmail(email: String) {
        _uiState.update { state ->
            state.copy(email = email)
        }
        validateEmail(email)
    }

    private fun validateEmail(email: String) {

        when(val result = userDataValidator.validateEmail(email)) {
            is AppResult.Success -> {
                _uiState.update { state ->
                    state.copy(
                        emailErrorMessage = null
                    )
                }
            }
            is AppResult.Failure -> {
                val message = when(result.error) {

                    EmailErrors.EMPTY_FIELD -> {
                        context.getString(R.string.empty_field_error)
                    }
                    EmailErrors.INVALID_EMAIL -> {
                        context.getString(R.string.invalid_email_error)
                    }
                }
                _uiState.update { state -> state.copy(emailErrorMessage = message) }
            }
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { state ->
            state.copy(isPasswordHidden = !state.isPasswordHidden)
        }
    }

    fun signUp(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val userId = accountService.signUp(
                    email = uiState.value.email,
                    password = uiState.value.password
                )

                val user = User(
                    id = userId,
                    username = uiState.value.displayName,
                    email = uiState.value.email
                )

                userRepository.saveUser(user)
                onSuccess()
            } catch (e: Exception) {

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
}
