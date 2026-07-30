package com.example.petvitals.ui.screens.userprofile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.AccountError
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.repository.UserRepository
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
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
): ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventChannel = Channel<UserProfileEvent>()
    val events = _eventChannel.receiveAsFlow()

    init {
        getUserData()
    }

    fun onAction(action: UserProfileAction) {
        when (action) {
            is UserProfileAction.Retry -> getUserData()
            is UserProfileAction.Logout -> logout()
            is UserProfileAction.DeleteAccount -> deleteAccount()
            is UserProfileAction.ShowModal -> showModal(action.show)
            is UserProfileAction.OnPasswordChange -> onPasswordChange(action.password)
            is UserProfileAction.SendPasswordResetEmail -> sendPasswordResetEmail()
        }
    }

    fun logout() {
        accountService.logout()
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

                val currentUserId = accountService.currentUserId ?: return@launch

                userRepository.deleteUser(currentUserId)
                when (val deleteResult = accountService.deleteAccount()) {
                    is AppResult.Success -> Unit
                    is AppResult.Failure -> {
                        _uiState.update { state ->
                            state.copy(passwordErrorMessage = deleteResult.error.toDeleteAccountErrorMessage())
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
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
        _uiState.update { state -> state.startLoading() }

        viewModelScope.launch {
            val currentUserId = accountService.currentUserId
            if (currentUserId == null) {
                _uiState.update { state ->
                    state.showError(R.string.session_expired_error)
                }
                return@launch
            }

            val user = when (val result = userRepository.getUserById(currentUserId)) {
                is AppResult.Success -> result.data
                is AppResult.Failure -> {
                    val errorMessageRes = when (result.error) {
                        FirestoreError.Network -> R.string.network_error
                        FirestoreError.PermissionDenied -> R.string.you_do_not_have_permission_to_access_this_data
                        FirestoreError.Unauthenticated -> R.string.session_expired_error
                        FirestoreError.Unknown -> R.string.unexpected_error
                    }
                    _uiState.update { state ->
                        state.showError(errorMessageRes)
                    }
                    return@launch
                }
            }

            if (user == null) {
                _uiState.update { state ->
                    state.showError(R.string.user_profile_not_found_error)
                }
            } else {
                _uiState.update { state -> state.showUser(user) }
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
