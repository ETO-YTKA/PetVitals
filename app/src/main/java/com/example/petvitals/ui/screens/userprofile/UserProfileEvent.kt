package com.example.petvitals.ui.screens.userprofile

import com.example.petvitals.ui.components.SnackbarState

sealed interface UserProfileEvent {
    data class OnShowSnackbar(val snackbarState: SnackbarState) : UserProfileEvent
}
