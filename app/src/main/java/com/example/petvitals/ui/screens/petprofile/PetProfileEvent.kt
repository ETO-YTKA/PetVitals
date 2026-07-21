package com.example.petvitals.ui.screens.petprofile

import androidx.annotation.StringRes
import com.example.petvitals.ui.components.SnackbarType

sealed interface PetProfileEvent {
    data class ShowSnackbar(
        @param:StringRes val messageRes: Int,
        val snackbarType: SnackbarType,
        val withDismissAction: Boolean = true
    ) : PetProfileEvent
    data object PetDeleted : PetProfileEvent
}
