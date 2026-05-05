package com.example.petvitals.ui.screens.signup

sealed interface SignUpAction {
    data class OnNameChanged(val name: String) : SignUpAction
    data class OnEmailChanged(val email: String) : SignUpAction
    data class OnPasswordChanged(val password: String) : SignUpAction
    data class OnRepeatPasswordChanged(val repeatPassword: String) : SignUpAction
    data object SignUp : SignUpAction
}
