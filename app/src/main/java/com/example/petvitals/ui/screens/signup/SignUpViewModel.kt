package com.example.petvitals.ui.screens.signup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.EmailErrors
import com.example.petvitals.domain.error.NameError
import com.example.petvitals.domain.error.PasswordError
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.repository.UserRepository
import com.example.petvitals.domain.validator.UserDataValidator
import com.example.petvitals.ui.components.SnackbarState
import com.example.petvitals.ui.components.SnackbarType
import com.example.petvitals.ui.utils.debounceValidation
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository,
    private val userDataValidator: UserDataValidator,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventChannel = Channel<SignUpEvent>()
    val events = _eventChannel.receiveAsFlow()

    private var nameValidationJob: Job? = null
    private var emailValidationJob: Job? = null
    private var passwordValidationJob: Job? = null
    private var confirmPasswordValidationJob: Job? = null

    fun onAction(
        action: SignUpAction,
        onSuccess: () -> Unit = {}
    ) {
        when (action) {
            is SignUpAction.OnNameChanged -> onNameChanged(action.name)
            is SignUpAction.OnEmailChanged -> onEmailChanged(action.email)
            is SignUpAction.OnPasswordChanged -> onPasswordChanged(action.password)
            is SignUpAction.OnRepeatPasswordChanged -> onRepeatPasswordChanged(action.repeatPassword)
            SignUpAction.SignUp -> signUp(onSuccess)
        }
    }

    private fun onNameChanged(displayName: String) {
        _uiState.update { state ->
            state.copy(
                name = displayName,
                nameErrorMessage = null
            )
        }

        nameValidationJob = debounceValidation(
            scope = viewModelScope,
            previousJob = nameValidationJob,
            validate = { validateName() }
        )
    }

    private fun onEmailChanged(email: String) {
        _uiState.update { state ->
            state.copy(
                email = email,
                emailErrorMessage = null
            )
        }

        emailValidationJob = debounceValidation(
            scope = viewModelScope,
            previousJob = emailValidationJob,
            validate = { validateEmail() }
        )
    }

    private fun onPasswordChanged(password: String) {
        _uiState.update { state ->
            state.copy(
                password = password,
                passwordErrorMessage = null
            )
        }

        passwordValidationJob = debounceValidation(
            scope = viewModelScope,
            previousJob = passwordValidationJob,
        ) {
            validatePassword()
        }

        confirmPasswordValidationJob = debounceValidation(
            scope = viewModelScope,
            previousJob = confirmPasswordValidationJob,
        ) {
            validateRepeatPassword()
        }
    }

    private fun onRepeatPasswordChanged(repeatPassword: String) {
        _uiState.update { state ->
            state.copy(repeatPassword = repeatPassword)
        }

        confirmPasswordValidationJob = debounceValidation(
            scope = viewModelScope,
            previousJob = confirmPasswordValidationJob,
        ) {
            validateRepeatPassword()
        }
    }

    private fun signUp(onSuccess: () -> Unit) {
        if (!isFieldsValid()) return

        viewModelScope.launch {
            try {
                val currentState = uiState.value
                val userId = accountService.signUp(
                    email = currentState.email,
                    password = currentState.password
                )

                val user = User(
                    id = userId,
                    username = currentState.name,
                    email = currentState.email
                )

                userRepository.saveUser(user)
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is IllegalArgumentException -> context.getString(R.string.empty_fields_error)
                    is FirebaseAuthInvalidCredentialsException -> context.getString(R.string.invalid_credentials_error)
                    is FirebaseAuthInvalidUserException -> context.getString(R.string.user_not_found_error)
                    is FirebaseNetworkException -> context.getString(R.string.network_error)
                    else -> context.getString(R.string.unexpected_error)
                }

                _eventChannel.send(
                    SignUpEvent.OnShowSnackbar(
                        snackbarState = SnackbarState(
                            message = errorMessage,
                            snackbarType = SnackbarType.ERROR
                        )
                    )
                )
            }
        }
    }

    private fun isFieldsValid(): Boolean {

        validateName()
        validateEmail()
        validatePassword()
        validateRepeatPassword()

        val uiState = uiState.value

        return uiState.nameErrorMessage == null &&
            uiState.emailErrorMessage == null &&
            uiState.passwordErrorMessage == null &&
            uiState.repeatPasswordErrorMessage == null
    }

    private fun validateName() {
        return when (val result = userDataValidator.validateName(uiState.value.name)) {

            is AppResult.Success -> {
                _uiState.update { state ->
                    state.copy(nameErrorMessage = null)
                }
            }
            is AppResult.Failure -> {
                val message = when (result.error) {
                    NameError.EMPTY_FIELD -> context.getString(R.string.empty_field_error)
                    NameError.TOO_LONG -> context.getString(R.string.display_name_too_long_error)
                    NameError.INVALID_CHARACTERS -> context.getString(R.string.invalid_characters_error)
                }
                _uiState.update { state -> state.copy(nameErrorMessage = message) }
            }
        }
    }

    private fun validateEmail() {
        return when (val result = userDataValidator.validateEmail(uiState.value.email)) {

            is AppResult.Success -> {
                _uiState.update { state ->
                    state.copy(emailErrorMessage = null)
                }
            }
            is AppResult.Failure -> {
                val message = when (result.error) {
                    EmailErrors.EMPTY_FIELD -> context.getString(R.string.empty_field_error)
                    EmailErrors.INVALID_EMAIL -> context.getString(R.string.invalid_email_error)
                }
                _uiState.update { state -> state.copy(emailErrorMessage = message) }
            }
        }
    }

    private fun validatePassword() {
        return when (val result = userDataValidator.validatePassword(uiState.value.password)) {

            is AppResult.Success -> {
                _uiState.update { state ->
                    state.copy(passwordErrorMessage = null)
                }
            }
            is AppResult.Failure -> {
                val message = when (result.error) {
                    PasswordError.EMPTY_FIELD -> context.getString(R.string.empty_field_error)
                    PasswordError.HAS_WHITESPACE -> context.getString(R.string.password_whitespace_error)
                    PasswordError.TOO_SHORT -> context.getString(R.string.password_short_error)
                    PasswordError.NO_DIGIT -> context.getString(R.string.password_no_digit_error)
                    PasswordError.NO_UPPERCASE -> context.getString(R.string.password_no_uppercase_error)
                    PasswordError.NO_LOWERCASE -> context.getString(R.string.password_no_lowercase_error)
                }
                _uiState.update { state -> state.copy(passwordErrorMessage = message) }
            }
        }
    }

    private fun validateRepeatPassword() {
        val password = uiState.value.password
        val repeatPassword = uiState.value.repeatPassword

        val message = when {
            repeatPassword != password -> context.getString(R.string.passwords_do_not_match_error)
            else -> null
        }

        _uiState.update { state ->
            state.copy(repeatPasswordErrorMessage = message)
        }
    }
}
