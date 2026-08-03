package com.example.petvitals.ui.screens.managefood

import com.example.petvitals.ui.components.SnackbarState

sealed interface ManageFoodEvent {
    data class OnShowSnackbar(val snackbarState: SnackbarState) : ManageFoodEvent
}