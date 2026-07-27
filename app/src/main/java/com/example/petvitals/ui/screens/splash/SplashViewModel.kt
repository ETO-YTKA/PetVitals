package com.example.petvitals.ui.screens.splash

import androidx.lifecycle.ViewModel
import com.example.petvitals.data.service.account.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {

    fun onAppStart(onNavigateToPets: () -> Unit, onNavigateToLogIn: () -> Unit) {
        if (accountService.hasUser() && accountService.isEmailVerified) onNavigateToPets()
        else onNavigateToLogIn()
    }
}