package com.example.petvitals.ui.screens.sign_up

import com.example.petvitals.LogIn
import com.example.petvitals.model.service.AccountService
import com.example.petvitals.ui.screens.PetVitalsAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.text.Regex

data class SignUpUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val isDisplayNameInvalid: Boolean = false,
    val isPasswordHidden: Boolean = true,
    val isPasswordInvalid: Boolean = false,
    val isEmailInvalid: Boolean = false,
    val signUpButtonEnabled: Boolean = false
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    val accountService: AccountService
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

    private fun validateDisplayName(name: String){
        val pattern = "^[A-Za-z]{2,30}$"
        val isNameInvalid = !Regex(pattern).containsMatchIn(name)
        _uiState.update {state -> state.copy(isDisplayNameInvalid = isNameInvalid) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { state ->
            state.copy(password = password)
        }
        validatePassword(password)
        updateSignUpButtonState()
    }

    private fun validatePassword(password: String) {
        val pattern = "^((?=\\S*?[A-Z])(?=\\S*?[a-z])(?=\\S*?[0-9]).{7,})\\S$"
        val isPasswordInvalid = !Regex(pattern).containsMatchIn(password)
        _uiState.update { state -> state.copy(isPasswordInvalid = isPasswordInvalid) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { state ->
            state.copy(email = email)
        }
        validateEmail(email)
        updateSignUpButtonState()
    }

    private fun validateEmail(email: String) {
        val pattern = "^((?!\\.)[\\w\\-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])\$"
        val isEmailInvalid = !Regex(pattern).containsMatchIn(email)
        _uiState.update { state -> state.copy(isEmailInvalid = isEmailInvalid) }
    }

    fun onChangeVisibilityClick() {
        _uiState.update { state ->
            state.copy(isPasswordHidden = !state.isPasswordHidden)
        }
    }

    fun onSignUpClick(navigateTo: (Any) -> Unit) {
        launchCatching {
            accountService.signUp(
                name = uiState.value.displayName,
                email = uiState.value.email,
                password = uiState.value.password
            )
            navigateTo(LogIn)
        }
    }

    private fun updateSignUpButtonState() {
        _uiState.update { state ->
            state.copy(signUpButtonEnabled = !uiState.value.isEmailInvalid && !uiState.value.isPasswordInvalid )
        }
    }
}