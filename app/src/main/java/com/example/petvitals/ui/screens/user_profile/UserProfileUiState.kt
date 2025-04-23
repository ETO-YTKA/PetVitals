package com.example.petvitals.ui.screens.user_profile

import androidx.lifecycle.viewModelScope
import com.example.petvitals.model.service.AccountService
import com.example.petvitals.ui.screens.PetVitalsAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileUiState(
    val displayName: String = "",
    val email: String = "",
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val accountService: AccountService
): PetVitalsAppViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun logout() {
        launchCatching {
            accountService.logout()
        }
    }

    fun getUserData() {
        viewModelScope.launch {
            val userData = accountService.getCurrentUserData()

            _uiState.update { state ->
                state.copy(
                    displayName = userData.getString("displayName") ?: "Anonymous",
                    email = userData.getString("email") ?: "wtf bro where's your email😭😭😭"
                )
            }
        }
    }
}