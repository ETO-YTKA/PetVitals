package com.example.petvitals.ui.screens.splash

import com.example.petvitals.Pets
import com.example.petvitals.LogIn
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.ui.screens.PetVitalsAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val accountService: AccountService
) : PetVitalsAppViewModel() {

    fun onAppStart(navigateTo: (Any) -> Unit) {
        if (accountService.hasUser()) navigateTo(Pets)
        else navigateTo(LogIn)
    }
}