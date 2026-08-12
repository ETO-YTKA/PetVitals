package com.example.petvitals.ui.screens.managerecord

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.Record
import com.example.petvitals.domain.models.RecordType
import com.example.petvitals.domain.models.canManagePetCare
import com.example.petvitals.domain.repository.PetRepository
import com.example.petvitals.domain.repository.RecordRepository
import com.example.petvitals.domain.usecase.GetCurrentUserRecords
import com.example.petvitals.ui.components.DropDownOption
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AddEditRecordUiState(
    val title: String = "",
    val selectedType: RecordType = RecordType.NOTE,
    val date: Date = Date(),
    val description: String = "",
    val selectedPets: List<Pet> = emptyList(),
    val originalPetIds: List<String> = emptyList(),
    val originalCreatedAt: Date? = null,
    val revision: Long = 0,

    val pets: List<Pet> = emptyList(),
    val typeOptions: List<DropDownOption<RecordType>> = emptyList(),

    val showBottomSheet: Boolean = false,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val isRecordLoading: Boolean = false,
    val isEditRecordLoaded: Boolean = false,
    val recordLoadErrorMessage: String? = null,
    val isSaving: Boolean = false,
    val saveErrorMessage: String? = null,

    val titleErrorMessage: String? = null,
    val descriptionErrorMessage: String? = null,
    val petSelectionErrorMessage: String? = null,

    val isTitleError: Boolean = false,
    val isDescriptionError: Boolean = false
)

@HiltViewModel
class AddEditRecordViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val petRepository: PetRepository,
    private val getCurrentUserRecords: GetCurrentUserRecords,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditRecordUiState())
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
        val date = Calendar.getInstance().apply {
            time = uiState.value.date
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
        }.time

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
        _uiState.update {
            it.copy(
                isRecordLoading = true,
                isEditRecordLoaded = false,
                recordLoadErrorMessage = null
            )
        }

        viewModelScope.launch {
            when (val result = getCurrentUserRecords()) {
                is AppResult.Failure -> setRecordLoadFailure()
                is AppResult.Success -> {
                    val overview = result.data.firstOrNull { it.record.id == recordId }
                    if (overview == null || !overview.canManage) {
                        setRecordLoadFailure()
                        return@launch
                    }

                    val record = overview.record
                    _uiState.update { state ->
                        state.copy(
                            title = record.title,
                            selectedType = record.type,
                            date = record.eventDate ?: record.createdAt,
                            description = record.description.orEmpty(),
                            selectedPets = overview.pets,
                            originalPetIds = record.petIds,
                            originalCreatedAt = record.createdAt,
                            revision = record.revision,
                            isRecordLoading = false,
                            isEditRecordLoaded = true,
                            recordLoadErrorMessage = null,
                            saveErrorMessage = null
                        )
                    }
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
                titleErrorMessage = null,
                petSelectionErrorMessage = null
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

        if (uiState.value.selectedPets.isEmpty()) {
            _uiState.update { state ->
                state.copy(
                    petSelectionErrorMessage = context.getString(
                        R.string.select_at_least_one_pet_error
                    )
                )
            }
            isValid = false
        }
        return isValid
    }

    fun saveRecord(recordId: String? = null, onSuccess: () -> Unit) {
        if (uiState.value.isSaving) return
        if (recordId != null && !uiState.value.isEditRecordLoaded) return
        if (!validateForm()) return

        val title = uiState.value.title.takeIf { it.isNotBlank() }
            ?: context.getString(uiState.value.selectedType.titleResId)

        val baseRecord = Record(
            title = title,
            type = uiState.value.selectedType,
            eventDate = uiState.value.date,
            createdAt = uiState.value.originalCreatedAt ?: Date(),
            description = uiState.value.description,
            petIds = uiState.value.selectedPets.map { pet -> pet.id },
            revision = uiState.value.revision
        )

        val record = when (recordId) {
            null -> baseRecord
            else -> baseRecord.copy(id = recordId)
        }

        _uiState.update { it.copy(isSaving = true, saveErrorMessage = null) }

        viewModelScope.launch {
            when (
                recordRepository.saveRecord(
                    record = record,
                    previousPetIds = uiState.value.originalPetIds
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess()
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveErrorMessage = context.getString(R.string.record_save_failed)
                        )
                    }
                    if (recordId != null) reloadRecordAfterSaveFailure(recordId)
                }
            }
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
            when (val result = petRepository.getCurrentUserPets()) {
                is AppResult.Failure -> Unit
                is AppResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            pets = result.data.filter {
                                it.currentUserPermission.canManagePetCare
                            }
                        )
                    }
                }
            }
        }
    }

    fun togglePetSelection(pet: Pet) {
        if (!pet.currentUserPermission.canManagePetCare) return

        _uiState.update { state ->
            state.copy(
                selectedPets = if (state.selectedPets.contains(pet)) {
                    state.selectedPets - pet
                } else {
                    state.selectedPets + pet
                },
                petSelectionErrorMessage = null
            )
        }
    }

    fun formatDateForDisplay(date: Date, context: Context): String {
        return date.let {
            SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault()).format(date)
        } ?: context.getString(R.string.tap_to_select_date)
    }

    private fun setRecordLoadFailure() {
        _uiState.update {
            it.copy(
                isRecordLoading = false,
                isEditRecordLoaded = false,
                recordLoadErrorMessage = context.getString(R.string.something_went_wrong_error)
            )
        }
    }

    private suspend fun reloadRecordAfterSaveFailure(recordId: String) {
        when (val result = getCurrentUserRecords()) {
            is AppResult.Failure -> Unit
            is AppResult.Success -> {
                val overview = result.data.firstOrNull { it.record.id == recordId }
                if (overview == null || !overview.canManage) {
                    setRecordLoadFailure()
                    return
                }

                val record = overview.record
                _uiState.update { state ->
                    state.copy(
                        title = record.title,
                        selectedType = record.type,
                        date = record.eventDate ?: record.createdAt,
                        description = record.description.orEmpty(),
                        selectedPets = overview.pets,
                        originalPetIds = record.petIds,
                        originalCreatedAt = record.createdAt,
                        revision = record.revision,
                        isEditRecordLoaded = true,
                        saveErrorMessage = context.getString(R.string.record_changed_reloaded)
                    )
                }
            }
        }
    }
}
