package com.example.petvitals.ui.screens.passwordreset

sealed interface PasswordResetAction {
    data class OnEmailChange(val email: String) : PasswordResetAction
    object OnSendPasswordResetEmail : PasswordResetAction
}