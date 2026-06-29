package com.example.petvitals.ui.screens.pets

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.repository.PetRepository
import com.example.petvitals.ui.utils.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetsViewModel @Inject constructor(
    private val accountService: AccountService,
    private val petRepository: PetRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshPets()
    }

    fun initialize(onNavigateToSplash: () -> Unit) {
        viewModelScope.launch {
            accountService.currentUser.collect { user ->
                if (user == null) onNavigateToSplash()
            }
        }
    }

    fun onAction(action: PetsAction) {
        when (action) {
            PetsAction.RefreshPets -> refreshPets()
        }
    }

    private fun refreshPets() {
        _uiState.update { state -> state.copy(isRefreshing = true) }

        viewModelScope.launch {
            val response = petRepository.getCurrentUserPets()

            when (response) {
                is AppResult.Success -> {
                    val pets = response.data.sortedBy { it.currentUserPermission }

                    _uiState.update { state ->
                        state.copy(
                            pets = pets,
                            isRefreshing = false
                        )
                    }
                }
                is AppResult.Failure -> {
                    val errorMessage = context.getString(response.error.toMessageRes())

                    _uiState.update { state ->
                        state.copy(
                            isRefreshing = false,
                            errorMessage = errorMessage
                        )
                    }
                }
            }
        }
    }
}