package com.example.petvitals.ui.screens.hallo

import com.example.petvitals.Splash
import com.example.petvitals.model.service.AccountService
import com.example.petvitals.ui.screens.PetVitalsAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class HalloUiState(
    val displayName: String = ""
)

@HiltViewModel
class HalloViewModel @Inject constructor(
    val accountService: AccountService
) : PetVitalsAppViewModel() {

    private val _uiState = MutableStateFlow(HalloUiState())
    val uiState = _uiState.asStateFlow()

    fun initialize(restartApp: (Any) -> Unit) {
        launchCatching {
            accountService.currentUser.collect { user ->
                if (user == null) restartApp(Splash)
                _uiState.update { state -> state.copy(displayName = accountService.currentUserName()) }
            }
        }
    }

    fun signOut() {
        launchCatching {
            accountService.signOut()
        }
    }
}