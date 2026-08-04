package com.example.petvitals.ui.screens.managemedication

import androidx.annotation.StringRes
import com.example.petvitals.ui.components.SnackbarType

sealed interface ManageMedicationEvent {
    data class OnShowSnackbar(
        @param:StringRes val messageRes: Int,
        val snackbarType: SnackbarType
    ) : ManageMedicationEvent
}
