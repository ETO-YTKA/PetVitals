package com.example.petvitals.ui.screens.splash

import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.ui.screens.PetVitalsAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val accountService: AccountService
) : PetVitalsAppViewModel() {

    fun onAppStart(onNavigateToPets: () -> Unit, onNavigateToLogIn: () -> Unit) {
        if (accountService.hasUser() && accountService.isEmailVerified) onNavigateToPets()
        else onNavigateToLogIn()
    }
}