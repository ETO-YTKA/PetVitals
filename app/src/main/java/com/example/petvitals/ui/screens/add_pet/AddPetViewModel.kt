package com.example.petvitals.ui.screens.add_pet

import android.content.Context
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet.PetRepository
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.ui.components.DropDownOption
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AddPetUiState(
    val name: String = "",
    val species: String = "",
    val isDateOfBirthApproximate: Boolean = false,
    val showModal: Boolean = false,
    val birthDateMillis: Long? = null,
    val selectedBirthMonth: Int = 0,
    val monthOptions: List<DropDownOption<Int>> = emptyList(),
    val birthYear: String = "",
)

@HiltViewModel
class AddPetViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val accountService: AccountService,
    @ApplicationContext private val context: Context
): ViewModel() {
    private val _uiState = MutableStateFlow(AddPetUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { state ->
            state.copy(monthOptions = populateMonthOptions(context = context))
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { state ->
            state.copy(name = name)
        }
    }

    fun onSpeciesChange(species: String) {
        _uiState.update { state ->
            state.copy(species = species)
        }
    }

    fun onDateOfBirthApproximateChange(isApproximate: Boolean) {
        _uiState.update { state ->
            state.copy(isDateOfBirthApproximate = isApproximate)
        }
    }

    fun onShowModalChange(show: Boolean) {
        _uiState.update { state ->
            state.copy(showModal = show)
        }
    }

    fun onBirthDateMillisChange(birthMillis: Long?) {
        _uiState.update { state ->
            state.copy(birthDateMillis = birthMillis)
        }
    }

    fun onBirthMonthChange(month: Int) {
        _uiState.update { state ->
            state.copy(selectedBirthMonth = month)
        }
    }

    fun onBirthYearChange(year: String) {
        _uiState.update { state ->
            state.copy(birthYear = year)
        }
    }

    fun formatDateForDisplay(millis: Long?, context: Context): String {
        return millis?.let {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
        } ?: context.getString(R.string.tap_to_select_date)
    }

    fun getSpeciesList(): List<String> {
        return listOf(
            "Cat",
            "Dog"
        )
    }

    fun populateMonthOptions(context: Context): List<DropDownOption<Int>> {
        return listOf(
            DropDownOption(
                display = context.getString(R.string.unknown),
                value = 0
            ),
            DropDownOption(
                display = context.getString(R.string.january),
                value = 1
            ),
            DropDownOption(
                display = context.getString(R.string.february),
                value = 2
            ),
            DropDownOption(
                display = context.getString(R.string.march),
                value = 3
            ),
            DropDownOption(
                display = context.getString(R.string.april),
                value = 4
            ),
            DropDownOption(
                display = context.getString(R.string.may),
                value = 5
            ),
            DropDownOption(
                display = context.getString(R.string.june),
                value = 6
            ),
            DropDownOption(
                display = context.getString(R.string.july),
                value = 7
            ),
            DropDownOption(
                display = context.getString(R.string.august),
                value = 8
            ),
            DropDownOption(
                display = context.getString(R.string.september),
                value = 9
            ),
            DropDownOption(
                display = context.getString(R.string.october),
                value = 10
            ),
            DropDownOption(
                display = context.getString(R.string.november),
                value = 11
            ),
            DropDownOption(
                display = context.getString(R.string.december),
                value = 12
            ))
    }

    fun addPet() {
        val userId = accountService.currentUserId

        val birthDate = if (uiState.value.isDateOfBirthApproximate) {
            when (uiState.value.selectedBirthMonth) {
                0 -> {
                    mapOf<String, Int>(
                        "year" to uiState.value.birthYear.toInt()
                    )
                }
                else -> {
                    mapOf<String, Int>(
                        "month" to uiState.value.selectedBirthMonth,
                        "year" to uiState.value.birthYear.toInt()
                    )
                }
            }
        } else {
            val date = Calendar.getInstance(Locale.getDefault())
            date.timeInMillis = uiState.value.birthDateMillis ?: 0

            mapOf<String, Int>(
                "day" to date.get(Calendar.DAY_OF_MONTH),
                "month" to date.get(Calendar.MONTH),
                "year" to date.get(Calendar.YEAR)
            )
        }

        viewModelScope.launch {
            try {
                petRepository.addPetToUser(
                    userId = userId,
                    petName = uiState.value.name,
                    species = uiState.value.species,
                    birthDate = birthDate
                )
            } catch (e: Exception) {
                Log.d("AddPetViewModel", "addPet: ${e.message}")
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    object PastOrPresentSelectableDates: SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return utcTimeMillis <= System.currentTimeMillis()
        }

        override fun isSelectableYear(year: Int): Boolean {
            return year <= LocalDate.now().year
        }
    }
}