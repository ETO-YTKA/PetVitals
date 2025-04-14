package com.example.petvitals.ui.screens.hallo

import com.example.petvitals.Splash
import com.example.petvitals.model.service.AccountService
import com.example.petvitals.ui.screens.PetVitalsAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HalloViewModel @Inject constructor(
    val accountService: AccountService
) : PetVitalsAppViewModel() {

    fun initialize(restartApp: (Any) -> Unit) {
        launchCatching {
            accountService.currentUser.collect { user ->
                if (user == null) restartApp(Splash)
            }
        }
    }

    fun signOut() {
        launchCatching {
            accountService.signOut()
        }
    }
}