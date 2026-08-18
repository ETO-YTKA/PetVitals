package com.example.petvitals.ui.screens.joinpet

import androidx.annotation.StringRes
import com.example.petvitals.ui.utils.INVITE_CODE_LENGTH

data class JoinPetUiState(
    val code: String = "",
    @param:StringRes val errorMessageRes: Int? = null,
    val isSubmitting: Boolean = false
) {
    val isCodeComplete: Boolean
        get() = code.length == INVITE_CODE_LENGTH
}