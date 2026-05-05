package com.example.petvitals.ui.screens.signup

import com.example.petvitals.ui.components.SnackbarState

sealed interface SignUpEvent {
    data class OnShowSnackbar(val snackbarState: SnackbarState) : SignUpEvent
}
