package com.example.petvitals.ui.screens.passwordreset

data class PasswordResetUiState(
    val email: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isPasswordResetEmailSent: Boolean = false
)
