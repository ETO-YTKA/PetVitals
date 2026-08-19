package com.example.petvitals.ui.screens.joinpet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.usecase.RedeemCodeUseCase
import com.example.petvitals.ui.utils.normalizeInviteCodeInput
import com.example.petvitals.ui.utils.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinPetViewModel @Inject constructor(
    private val redeemCodeUseCase: RedeemCodeUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(JoinPetUiState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: JoinPetAction) {
        when (action) {
            is JoinPetAction.OnCodeChange -> onCodeChange(action.code)
            JoinPetAction.OnJoinPetClick -> onJoinPetScreen()
        }
    }

    private fun onCodeChange(code: String) {
        _uiState.update { state ->
            state.copy(code = normalizeInviteCodeInput(code))
        }
    }

    private fun onJoinPetScreen() {
        _uiState.update { state ->
            state.copy(isSubmitting = true)
        }

        viewModelScope.launch {
            when (val result = redeemCodeUseCase.invoke(uiState.value.code)) {
                is AppResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            errorMessageRes = result.error.toMessageRes(),
                            isSubmitting = false
                        )
                    }
                }
                is AppResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false
                        )
                    }
                }
            }
        }
    }
}