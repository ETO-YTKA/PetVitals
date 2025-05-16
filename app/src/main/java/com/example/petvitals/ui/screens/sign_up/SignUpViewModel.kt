package com.example.petvitals.ui.screens.sign_up

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.repository.user.User
import com.example.petvitals.data.repository.user.UserRepository
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.Result
import com.example.petvitals.domain.SignUpDataValidator
import com.example.petvitals.ui.screens.PetVitalsAppViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
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
    val isDisplayNameInvalid: Boolean = false,
    val isEmailInvalid: Boolean = false,
    val isPasswordInvalid: Boolean = false,
    val isPasswordHidden: Boolean = true,
    val displayNameErrorMessage: String? = null,
    val emailErrorMessage: String? = null,
    val passwordErrorMessage: String? = null,
    val signUpErrorMessage: String? = null,
    val signUpButtonEnabled: Boolean = false
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    val accountService: AccountService,
    val userRepository: UserRepository,
    @ApplicationContext private val context: Context,
    val dataValidator: SignUpDataValidator
) : PetVitalsAppViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    fun onDisplayNameChange(name: String) {
        _uiState.update { state ->
            state.copy(displayName = name)
        }
        validateDisplayName(name)
        updateSignUpButtonState()
    }

    private fun validateDisplayName(displayName: String){
        when(val result = dataValidator.validateDisplayName(displayName)) {

            is Result.Error -> {
                _uiState.update { state -> state.copy(isDisplayNameInvalid = true) }

                when(result.error) {
                    SignUpDataValidator.DisplayNameError.EMPTY_FIELD -> _uiState.update { state ->
                        state.copy(displayNameErrorMessage = context.getString(R.string.empty_field_error))
                    }
                    SignUpDataValidator.DisplayNameError.TOO_LONG -> _uiState.update { state ->
                        state.copy(displayNameErrorMessage = context.getString(R.string.display_name_too_long_error))
                    }
                    SignUpDataValidator.DisplayNameError.INVALID_CHARACTERS -> _uiState.update { state ->
                        state.copy(displayNameErrorMessage = context.getString(R.string.invalid_characters_error))
                    }
                }
            }
            is Result.Success -> _uiState.update { state ->
                state.copy(
                    isDisplayNameInvalid = false,
                    displayNameErrorMessage = null
                )
            }
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { state ->
            state.copy(password = password)
        }
        validatePassword(password)
        updateSignUpButtonState()
    }

    private fun validatePassword(password: String) {
        when(val result = dataValidator.validatePassword(password)) {

            is Result.Error -> {
                _uiState.update { state -> state.copy(isPasswordInvalid = true) }

                when(result.error) {
                    SignUpDataValidator.PasswordError.EMPTY_FIELD -> _uiState.update { state ->
                        state.copy(passwordErrorMessage = context.getString(R.string.empty_field_error))
                    }
                    SignUpDataValidator.PasswordError.HAS_WHITESPACE -> _uiState.update { state ->
                        state.copy(passwordErrorMessage = context.getString(R.string.password_whitespace_error))
                    }
                    SignUpDataValidator.PasswordError.TOO_SHORT -> _uiState.update { state ->
                        state.copy(passwordErrorMessage = context.getString(R.string.password_short_error))
                    }
                    SignUpDataValidator.PasswordError.NO_DIGIT -> _uiState.update { state ->
                        state.copy(passwordErrorMessage = context.getString(R.string.password_no_digit_error))
                    }
                    SignUpDataValidator.PasswordError.NO_UPPERCASE -> _uiState.update { state ->
                        state.copy(passwordErrorMessage = context.getString(R.string.password_no_uppercase_error))
                    }
                    SignUpDataValidator.PasswordError.NO_LOWERCASE -> _uiState.update { state ->
                        state.copy(passwordErrorMessage = context.getString(R.string.password_no_lowercase_error))
                    }
                }
            }
            is Result.Success -> _uiState.update { state ->
                state.copy(
                    isPasswordInvalid = false,
                    passwordErrorMessage = null
                )
            }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { state ->
            state.copy(email = email)
        }
        validateEmail(email)
        updateSignUpButtonState()
    }

    private fun validateEmail(email: String) {
        when(val result = dataValidator.validateEmail(email)) {

            is Result.Error -> {
                _uiState.update { state -> state.copy(isEmailInvalid = true) }

                when(result.error) {
                    SignUpDataValidator.EmailErrors.EMPTY_FIELD -> _uiState.update { state ->
                        state.copy(emailErrorMessage = context.getString(R.string.empty_field_error))
                    }
                    SignUpDataValidator.EmailErrors.INVALID_EMAIL -> _uiState.update { state ->
                        state.copy(emailErrorMessage = context.getString(R.string.invalid_email_error))
                    }
                }
            }
            is Result.Success -> _uiState.update { state ->
                state.copy(
                    isEmailInvalid = false,
                    emailErrorMessage = null
                )
            }
        }
    }

    fun onChangeVisibilityClick() {
        _uiState.update { state ->
            state.copy(isPasswordHidden = !state.isPasswordHidden)
        }
    }

    fun onSignUpClick(onNavigateToLogIn: () -> Unit) {
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

                userRepository.createUserDocument(user)

                onNavigateToLogIn()
            } catch (e: Exception) {
                Log.d("SignUpViewModel", "Sign up failed", e)

                val errorMessage = when(e) {
                    is FirebaseNetworkException -> context.getString(R.string.network_error)
                    is FirebaseAuthUserCollisionException -> context.getString(R.string.email_already_in_use_error)
                    else -> context.getString(R.string.unexpected_error)
                }

                _uiState.update { state ->
                    state.copy(signUpErrorMessage = errorMessage)
                }
            }
        }
    }

    private fun updateSignUpButtonState() {
        _uiState.update { state ->
            state.copy(signUpButtonEnabled = !uiState.value.isEmailInvalid && !uiState.value.isPasswordInvalid && !uiState.value.isDisplayNameInvalid )
        }
    }
}