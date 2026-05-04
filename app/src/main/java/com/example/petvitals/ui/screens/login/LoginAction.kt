package com.example.petvitals.ui.screens.login

sealed interface LoginAction {
    data class OnEmailChanged(val email: String) : LoginAction
    data class OnPasswordChanged(val password: String) : LoginAction
    data object Authenticate : LoginAction
    data object SendVerificationEmail : LoginAction
}
