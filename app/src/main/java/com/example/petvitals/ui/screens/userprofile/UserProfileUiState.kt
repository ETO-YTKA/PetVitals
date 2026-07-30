package com.example.petvitals.ui.screens.userprofile

import androidx.annotation.StringRes
import com.example.petvitals.domain.models.User

data class UserProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    @param:StringRes val errorMessageRes: Int? = null,

    //Modal
    val showDeleteAccountModal: Boolean = false,
    val password: String = "",
    val passwordErrorMessage: String? = null
)

internal fun UserProfileUiState.startLoading(): UserProfileUiState = copy(
    user = null,
    isLoading = true,
    errorMessageRes = null
)

internal fun UserProfileUiState.showUser(user: User): UserProfileUiState = copy(
    user = user,
    isLoading = false,
    errorMessageRes = null
)

internal fun UserProfileUiState.showError(
    @StringRes errorMessageRes: Int
): UserProfileUiState = copy(
    user = null,
    isLoading = false,
    errorMessageRes = errorMessageRes
)
