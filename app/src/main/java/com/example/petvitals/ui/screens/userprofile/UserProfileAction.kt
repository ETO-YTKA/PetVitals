package com.example.petvitals.ui.screens.userprofile

sealed interface UserProfileAction {
    object Retry : UserProfileAction
    object Logout : UserProfileAction
    object DeleteAccount : UserProfileAction
    object SendPasswordResetEmail : UserProfileAction
    data class ShowModal(val show: Boolean) : UserProfileAction
    data class OnPasswordChange(val password: String) : UserProfileAction
}