package com.example.petvitals.ui.screens.passwordreset

import com.example.petvitals.ui.components.SnackbarState

sealed interface PasswordResetEvent {
    data class OnShowSnackbar(val snackbarState: SnackbarState) : PasswordResetEvent
}