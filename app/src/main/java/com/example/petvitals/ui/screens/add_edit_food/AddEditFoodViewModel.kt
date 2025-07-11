package com.example.petvitals.ui.screens.add_edit_food

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.repository.food.Food
import com.example.petvitals.data.repository.food.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEditFoodUiState(
    val isLoading: Boolean = false,

    val petId: String = "",
    val foodId: String? = null,
    val name: String = "",
    val portion: String = "",
    val frequency: String = "",
    val note: String = "",

    val nameErrorMessage: String? = null,
    val portionErrorMessage: String? = null,
    val frequencyErrorMessage: String? = null,
    val noteErrorMessage: String? = null,
    val saveErrorMessage: String? = null
)

@HiltViewModel
class AddEditFoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddEditFoodUiState())
    val uiState = _uiState.asStateFlow()

    fun loadInitialData(petId: String, foodId: String?) {
        _uiState.update { state ->
            state.copy(
                petId = petId,
                foodId = foodId,
                isLoading = true
            )
        }

        viewModelScope.launch {
            foodId?.let { foodId ->
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
                }
            }
            _uiState.update { state ->
                state.copy(
                    isLoading = false
                )
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { state ->
            state.copy(
                name = value,
                nameErrorMessage = validateName(value)
            )
        }
    }

    private fun validateName(value: String = uiState.value.name): String? {
        return when {
            value.isBlank() -> context.getString(R.string.food_name_cannot_be_empty_error)
            value.length > 50 -> context.getString(R.string.food_name_too_long_error)
            else -> null
        }
    }

    fun onPortionChange(value: String) {
        _uiState.update { state ->
            state.copy(
                portion = value,
                portionErrorMessage = validatePortion(value)
            )
        }
    }

    private fun validatePortion(value: String = uiState.value.portion): String? {
        return when {
            value.isBlank() -> context.getString(R.string.food_portion_cannot_be_empty_error)
            value.length > 50 -> context.getString(R.string.food_portion_too_long_error)
            else -> null
        }
    }

    fun onFrequencyChange(value: String) {
        _uiState.update { state ->
            state.copy(
                frequency = value,
                frequencyErrorMessage = validateFrequency(value)
            )
        }
    }

    private fun validateFrequency(value: String = uiState.value.frequency): String? {
        return when {
            value.isBlank() -> context.getString(R.string.frequency_cannot_be_empty_error)
            value.length > 50 -> context.getString(R.string.food_frequency_too_long_error)
            else -> null
        }
    }

    fun onNoteChange(value: String) {
        _uiState.update { state ->
            state.copy(
                note = value,
                noteErrorMessage = validateNote(value)
            )
        }
    }

    private fun validateNote(value: String = uiState.value.note): String? {
        return when {
            value.length > 500 -> context.getString(R.string.food_note_too_long_error)
            else -> null
        }
    }

    private fun isFormValid(): Boolean {

        _uiState.update { it.copy(
            nameErrorMessage = validateName(),
            portionErrorMessage = validatePortion(),
            frequencyErrorMessage = validateFrequency(),
            noteErrorMessage = validateNote()
        )}

        return uiState.value.nameErrorMessage == null &&
                uiState.value.portionErrorMessage == null &&
                uiState.value.frequencyErrorMessage == null &&
                uiState.value.noteErrorMessage == null
    }

    fun save(onSuccess: () -> Unit) {
        if (!isFormValid()) return

        viewModelScope.launch {
            val baseFood = Food(
                petId = uiState.value.petId,
                name = uiState.value.name,
                portion = uiState.value.portion,
                frequency = uiState.value.frequency,
                note = uiState.value.note
            )

            val food = when (uiState.value.foodId) {
                null -> baseFood
                else -> baseFood.copy(id = uiState.value.foodId!!)
            }

            try {
                foodRepository.saveFood(food)
                Toast.makeText(
                    context,
                    context.getString(R.string.food_saved_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                onSuccess()
            } catch (e: Exception) {
                Log.d("AddEditFoodViewModel", e.message.orEmpty())
                Toast.makeText(
                    context,
                    context.getString(R.string.something_went_wrong_please_try_again_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}