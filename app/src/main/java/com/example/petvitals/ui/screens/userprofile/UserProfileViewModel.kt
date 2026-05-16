package com.example.petvitals.ui.screens.userprofile

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.AccountError
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.repository.UserRepository
import com.example.petvitals.ui.components.SnackbarState
import com.example.petvitals.ui.components.SnackbarType
import com.example.petvitals.ui.screens.PetVitalsAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileUiState(
    val username: String = "",
    val email: String = "",

    //Modal
    val showDeleteAccountModal: Boolean = false,
    val password: String = "",
    val passwordErrorMessage: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
): PetVitalsAppViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventChannel = Channel<UserProfileEvent>()
    val events = _eventChannel.receiveAsFlow()

    init {
        getUserData()
    }

    fun logout() {
        launchCatching {
            accountService.logout()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                val password = uiState.value.password
                val email = accountService.currentUserEmail ?: ""

                when (val signInResult = accountService.signIn(email, password)) {
                    is AppResult.Success -> Unit
                    is AppResult.Failure -> {
                        _uiState.update { state ->
                            state.copy(passwordErrorMessage = signInResult.error.toDeleteAccountErrorMessage())
                        }
                        return@launch
                    }
                }

                userRepository.deleteCurrentUser()
                when (val deleteResult = accountService.deleteAccount()) {
                    is AppResult.Success -> Unit
                    is AppResult.Failure -> {
                        _uiState.update { state ->
                            state.copy(passwordErrorMessage = deleteResult.error.toDeleteAccountErrorMessage())
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(passwordErrorMessage = context.getString(R.string.unexpected_error))
                }
            }
        }
    }

    fun showModal(show: Boolean) {
        _uiState.update { state ->
            state.copy(showDeleteAccountModal = show)
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { state -> state.copy(password = password) }
    }

    fun getUserData() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: User()

            _uiState.update { state ->
                state.copy(
                    username = user.username,
                    email = user.email
                )
            }
        }
    }

    fun sendPasswordResetEmail() {
        viewModelScope.launch {
            val email = accountService.currentUserEmail ?: ""
            when (val result = accountService.sendPasswordResetEmail(email)) {
                is AppResult.Success -> {
                    showSnackbar(
                        message = context.getString(R.string.password_reset_email_sent),
                        snackbarType = SnackbarType.SUCCESS
                    )
                }
                is AppResult.Failure -> {
                    showSnackbar(
                        message = result.error.toPasswordResetErrorMessage(),
                        snackbarType = SnackbarType.ERROR
                    )
                }
            }
        }
    }

    private suspend fun showSnackbar(
        message: String,
        snackbarType: SnackbarType
    ) {
        _eventChannel.send(
            UserProfileEvent.OnShowSnackbar(
                snackbarState = SnackbarState(
                    message = message,
                    snackbarType = snackbarType
                )
            )
        )
    }

    private fun AccountError.toDeleteAccountErrorMessage(): String = when (this) {
        AccountError.EmptyFields -> context.getString(R.string.empty_fields_error)
        AccountError.InvalidCredentials -> context.getString(R.string.incorrect_password_error)
        AccountError.Network -> context.getString(R.string.network_error)
        else -> context.getString(R.string.unexpected_error)
    }

    private fun AccountError.toPasswordResetErrorMessage(): String = when (this) {
        AccountError.Network -> context.getString(R.string.network_error)
        else -> context.getString(R.string.failed_to_send_password_reset_email)
    }
}
