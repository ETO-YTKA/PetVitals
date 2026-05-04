package com.example.petvitals.ui.screens.login

import com.example.petvitals.ui.components.SnackbarState

sealed interface LoginEvent {
    data class OnError(val snackbarState: SnackbarState) : LoginEvent
}
