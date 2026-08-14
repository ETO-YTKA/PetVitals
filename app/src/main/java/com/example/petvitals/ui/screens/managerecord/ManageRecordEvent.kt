package com.example.petvitals.ui.screens.managerecord

import androidx.annotation.StringRes
import com.example.petvitals.ui.components.SnackbarType

sealed interface ManageRecordEvent {
    data class OnShowSnackbar(
        @param:StringRes val messageRes: Int,
        val snackbarType: SnackbarType
    ) : ManageRecordEvent
}