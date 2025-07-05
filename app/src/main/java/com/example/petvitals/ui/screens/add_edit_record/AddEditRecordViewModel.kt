package com.example.petvitals.ui.screens.add_edit_record

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetRepository
import com.example.petvitals.data.repository.pet_permission.PermissionLevel
import com.example.petvitals.data.repository.pet_permission.PetPermissionRepository
import com.example.petvitals.data.repository.record.Record
import com.example.petvitals.data.repository.record.RecordRepository
import com.example.petvitals.data.repository.record.RecordType
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.ui.components.DropDownOption
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class CreateRecordUiState(
    val title: String = "",
    val selectedType: RecordType = RecordType.NOTE,
    val date: Date = Date(),
    val description: String = "",
    val selectedPets: List<Pet> = emptyList(),

    val pets: List<Pet> = emptyList(),
    val typeOptions: List<DropDownOption<RecordType>> = emptyList(),

    val showBottomSheet: Boolean = false,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,

    val titleErrorMessage: String? = null,
    val descriptionErrorMessage: String? = null,

    val isTitleError: Boolean = false,
    val isDescriptionError: Boolean = false
)

@HiltViewModel
class AddEditRecordViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val accountService: AccountService,
    private val petRepository: PetRepository,
    private val petPermissionRepository: PetPermissionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRecordUiState())
    val uiState = _uiState.asStateFlow()

    init {
        populateTypeOptions()
        getPets()
    }

    fun onTitleChange(title: String) {
        _uiState.update { state ->
            state.copy(title = title)
        }
    }

    fun onTypeChange(type: RecordType) {
        _uiState.update { state ->
            state.copy(selectedType = type)
        }
    }

    fun onShowDatePickerChange(show: Boolean) {
        _uiState.update { state ->
            state.copy(showDatePicker = show)
        }
    }

    fun onShowTimePickerChange(show: Boolean) {
        _uiState.update { state ->
            state.copy(showTimePicker = show)
        }
    }

    fun onDateChange(date: Long?) {
        if (date == null) return

        _uiState.update { state ->
            state.copy(
                date = Date(date),
                showDatePicker = false,
                showTimePicker = true
            )
        }
    }

    fun onTimeChange(hours: Int, minutes: Int) {
        val date = uiState.value.date
        date.hours = hours
        date.minutes = minutes

        _uiState.update { state ->
            state.copy(
                date = date,
                showTimePicker = false
            )
        }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { state ->
            state.copy(description = description)
        }
    }

    fun onShowBottomSheetChange(show: Boolean) {
        _uiState.update { state ->
            state.copy(showBottomSheet = show)
        }
    }

    fun loadRecordData(recordId: String) {
        viewModelScope.launch {
            val record = recordRepository.getRecordById(recordId)
            if (record != null) {
                _uiState.update { state ->
                    state.copy(
                        title = record.title,
                        selectedType = record.type,
                        date = record.date,
                        description = record.description,
                        selectedPets = record.petIds.mapNotNull { petId ->
                            petRepository.getPetById(petId)
                        }
                    )
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val title = uiState.value.title
        val description = uiState.value.description

        _uiState.update { state ->
            state.copy(
                isDescriptionError = false,
                isTitleError = false,
                descriptionErrorMessage = null,
                titleErrorMessage = null
            )
        }


        if (title.length > 50) {
            _uiState.update { state ->
                state.copy(
                    titleErrorMessage = context.getString(R.string.title_too_long),
                    isTitleError = true
                )
            }
            isValid = false
        }

        if (description.length > 500) {
            _uiState.update { state ->
                state.copy(
                    descriptionErrorMessage = context.getString(R.string.description_cannot_be_longer_than_error),
                    isDescriptionError = true
                )
            }
            isValid = false
        }
        return isValid
    }

    fun saveRecord(recordId: String? = null, onSuccess: () -> Unit) {
        if (!validateForm()) return

        val userId = accountService.currentUserId
        val title = uiState.value.title.takeIf { it.isNotBlank() }
            ?: context.getString(uiState.value.selectedType.titleResId)

        val baseRecord = Record(
            userId = userId,
            title = title,
            type = uiState.value.selectedType,
            date = uiState.value.date,
            description = uiState.value.description,
            petIds = uiState.value.selectedPets.map { pet -> pet.id },
        )

        val record = when (recordId) {
            null -> baseRecord
            else -> baseRecord.copy(id = recordId)
        }

        viewModelScope.launch {
            recordRepository.saveRecord(record)
            onSuccess()
        }
    }

    fun populateTypeOptions() {
        _uiState.update { state ->
            state.copy(
                typeOptions = listOf(
                    DropDownOption(
                        display = context.getString(RecordType.NOTE.titleResId),
                        value = RecordType.NOTE
                    ),
                    DropDownOption(
                        display = context.getString(RecordType.VACCINATION.titleResId),
                        value = RecordType.VACCINATION
                    ),
                    DropDownOption(
                        display = context.getString(RecordType.MEDICATION.titleResId),
                        value = RecordType.MEDICATION
                    ),
                    DropDownOption(
                        display = context.getString(RecordType.VET_VISIT.titleResId),
                        value = RecordType.VET_VISIT
                    ),
                    DropDownOption(
                        display = context.getString(RecordType.SYMPTOM.titleResId),
                        value = RecordType.SYMPTOM
                    ),
                    DropDownOption(
                        display = context.getString(RecordType.GROOMING.titleResId),
                        value = RecordType.GROOMING
                    ),
                    DropDownOption(
                        display = context.getString(RecordType.INCIDENT.titleResId),
                        value = RecordType.INCIDENT
                    )
                )
            )
        }
    }

    fun getPets() {
        viewModelScope.launch {
            val pets = petPermissionRepository.getCurrentUserPets().mapNotNull { petPermission ->
                val pet = petRepository.getPetById(petPermission.petId)
                if (petPermission.permissionLevel == PermissionLevel.VIEWER) {
                    return@mapNotNull null
                } else pet
            }
            _uiState.update { state ->
                state.copy(
                    pets = pets
                )
            }
        }
    }

    fun onPetSelected(pet: Pet) {
        _uiState.update { state ->
            state.copy(
                selectedPets = if (state.selectedPets.contains(pet)) {
                    state.selectedPets - pet
                } else {
                    state.selectedPets + pet
                }
            )
        }
    }

    fun formatDateForDisplay(date: Date, context: Context): String {
        return date.let {
            SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault()).format(date)
        } ?: context.getString(R.string.tap_to_select_date)
    }
}