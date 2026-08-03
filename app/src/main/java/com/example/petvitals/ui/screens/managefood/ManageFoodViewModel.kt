package com.example.petvitals.ui.screens.managefood

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.models.Food
import com.example.petvitals.domain.repository.FoodRepository
import com.example.petvitals.domain.usecase.SaveFoodUseCase
import com.example.petvitals.domain.validator.FoodDataValidator
import com.example.petvitals.ui.components.SnackbarState
import com.example.petvitals.ui.components.SnackbarType
import com.example.petvitals.ui.utils.toMessageRes
import com.example.petvitals.ui.utils.toMessageResOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel
class ManageFoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val saveFoodUseCase: SaveFoodUseCase,
    private val foodValidator: FoodDataValidator,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageFoodUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventChannel = Channel<ManageFoodEvent>()
    val events = _eventChannel.receiveAsFlow()

    fun onAction(action: ManageFoodAction) {
        when (action) {
            is ManageFoodAction.OnNameChange -> onNameChange(action.name)
            is ManageFoodAction.OnPortionChange -> onPortionChange(action.portion)
            is ManageFoodAction.OnFrequencyChange -> onFrequencyChange(action.frequency)
            is ManageFoodAction.OnNoteChange -> onNoteChange(action.note)
            is ManageFoodAction.OnSave -> onSave(action.onSuccess)
        }
    }

    fun loadInitialData(petId: String, foodId: String?) {
        _uiState.update { state ->
            state.copy(
                petId = petId,
                foodId = foodId,
                isLoading = foodId != null
            )
        }

        if (foodId == null) return

        viewModelScope.launch {
            val food = foodRepository.getFoodById(petId, foodId)
            if (food != null) {
                _uiState.update { state ->
                    state.copy(
                        name = food.name,
                        portion = food.portion,
                        frequency = food.frequency,
                        note = food.note,
                        isLoading = false
                    )
                }
                return@launch
            }

            _uiState.update { state ->
                state.copy(
                    isLoading = false
                )
            }
        }
    }

    private fun onNameChange(value: String) {
        _uiState.update { state ->
            state.copy(
                name = value
            )
        }
        validateName(value)
    }

    private fun onPortionChange(value: String) {
        _uiState.update { state ->
            state.copy(
                portion = value
            )
        }
        validatePortion(value)
    }

    private fun onFrequencyChange(value: String) {
        _uiState.update { state ->
            state.copy(
                frequency = value
            )
        }
        validateFrequency(value)
    }

    private fun onNoteChange(value: String) {
        _uiState.update { state ->
            state.copy(
                note = value
            )
        }
        validateNote(value)
    }

    private fun onSave(onSuccess: () -> Unit) {
        if (!isFormValid()) return

        val uiState = uiState.value
        val food = Food(
            id = uiState.foodId ?: UUID.randomUUID().toString(),
            petId = uiState.petId,
            name = uiState.name,
            portion = uiState.portion,
            frequency = uiState.frequency,
            note = uiState.note
        )

        viewModelScope.launch {
            when (val result = saveFoodUseCase.invoke(food)) {
                is AppResult.Success -> onSuccess()
                is AppResult.Failure -> {
                    showSnackbar(
                        message = context.getString(result.error.toMessageRes()),
                        snackbarType = SnackbarType.ERROR
                    )
                }
            }
        }
    }

    private fun validateName(name: String) {
        val result = foodValidator.validateName(name).toMessageResOrNull()

        _uiState.update { state ->
            state.copy(
                nameErrorMessageRes = result
            )
        }
    }

    private fun validatePortion(portion: String) {
        val result = foodValidator.validatePortion(portion).toMessageResOrNull()

        _uiState.update { state ->
            state.copy(
                portionErrorMessageRes = result
            )
        }
    }

    private fun validateFrequency(frequency: String) {
        val result = foodValidator.validateFrequency(frequency).toMessageResOrNull()

        _uiState.update { state ->
            state.copy(
                frequencyErrorMessageRes = result
            )
        }
    }

    private fun validateNote(note: String) {
        val result = foodValidator.validateNote(note).toMessageResOrNull()

        _uiState.update { state ->
            state.copy(
                noteErrorMessageRes = result
            )
        }
    }

    private fun isFormValid(): Boolean {

        val nameErrorMessageRes = foodValidator.validateName(uiState.value.name).toMessageResOrNull()
        val portionErrorMessageRes = foodValidator.validatePortion(uiState.value.portion).toMessageResOrNull()
        val frequencyErrorMessageRes = foodValidator.validateFrequency(uiState.value.frequency).toMessageResOrNull()
        val noteErrorMessageRes = foodValidator.validateNote(uiState.value.note).toMessageResOrNull()

        _uiState.update { state ->
            state.copy(
                nameErrorMessageRes = nameErrorMessageRes,
                portionErrorMessageRes = portionErrorMessageRes,
                frequencyErrorMessageRes = frequencyErrorMessageRes,
                noteErrorMessageRes = noteErrorMessageRes
            )
        }

        return listOf(
            nameErrorMessageRes,
            portionErrorMessageRes,
            frequencyErrorMessageRes,
            noteErrorMessageRes
        ).all { it == null }
    }

    private suspend fun showSnackbar(
        message: String,
        snackbarType: SnackbarType
    ) {
        _eventChannel.send(
            ManageFoodEvent.OnShowSnackbar(
                snackbarState = SnackbarState(
                    message = message,
                    snackbarType = snackbarType
                )
            )
        )
    }
}