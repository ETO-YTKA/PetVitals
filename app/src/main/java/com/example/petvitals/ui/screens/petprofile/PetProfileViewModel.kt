package com.example.petvitals.ui.screens.petprofile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Food
import com.example.petvitals.domain.models.Medication
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.canDeletePet
import com.example.petvitals.domain.models.canManagePetCare
import com.example.petvitals.domain.repository.FoodRepository
import com.example.petvitals.domain.repository.MedicationRepository
import com.example.petvitals.domain.repository.PetRepository
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import com.example.petvitals.ui.components.SnackbarType
import com.example.petvitals.ui.utils.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DateTimeException
import java.time.LocalDate
import java.time.Month
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PetProfileViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val medicationRepository: MedicationRepository,
    private val foodRepository: FoodRepository,
    private val getPetPermission: GetPetPermissionUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventChannel = Channel<PetProfileEvent>()
    val events = _eventChannel.receiveAsFlow()

    fun onAction(action: PetProfileAction) {
        when (action) {
            is PetProfileAction.LoadPet -> getPetData(action.petId)
            PetProfileAction.ToggleDeleteModal -> toggleOnDeleteModal()
            is PetProfileAction.EditNote -> editNote(action.noteType)
            PetProfileAction.CancelNoteEdit -> cancelNoteEdit()
            is PetProfileAction.OnNoteChange -> onNoteChange(action.value)
            PetProfileAction.SaveNote -> saveNote()
            is PetProfileAction.DeletePet -> deletePet(action.petId)
            is PetProfileAction.DeleteMedication -> onDeleteMedicationClick(action.medication)
            is PetProfileAction.DeleteFood -> onDeleteFoodClick(action.food)
        }
    }

    private fun toggleOnDeleteModal() {
        _uiState.update { state ->
            if (state.permissionLevel.canDeletePet) {
                state.copy(showOnDeleteModal = !state.showOnDeleteModal)
            } else {
                state
            }
        }
    }

    private fun editNote(noteType: PetNoteType) {
        _uiState.update { state ->
            if (!state.permissionLevel.canManagePetCare || state.noteEditor.isSaving) {
                state
            } else {
                val content = when (noteType) {
                    PetNoteType.HEALTH -> state.pet.healthNote
                    PetNoteType.FOOD -> state.pet.foodNote
                }
                state.copy(noteEditor = state.noteEditor.open(noteType, content))
            }
        }
    }

    private fun cancelNoteEdit() {
        _uiState.update { state ->
            if (state.noteEditor.isSaving) {
                state
            } else {
                state.copy(noteEditor = state.noteEditor.cancel())
            }
        }
    }

    private fun onNoteChange(value: String) {
        _uiState.update { state ->
            if (state.noteEditor.noteType == null || state.noteEditor.isSaving) {
                state
            } else {
                state.copy(noteEditor = state.noteEditor.updateDraft(value))
            }
        }
    }

    private fun onDeleteMedicationClick(medication: Medication) {
        val state = uiState.value
        if (!state.permissionLevel.canManagePetCare || medication.petId != state.pet.id) return

        viewModelScope.launch {
            when (val result = medicationRepository.deleteMedication(medication)) {
                is AppResult.Success -> _uiState.update { currentState ->
                    currentState.copy(
                        medications = currentState.medications.filterNot { it.id == medication.id }
                    )
                }
                is AppResult.Failure -> _eventChannel.send(
                    PetProfileEvent.ShowSnackbar(
                        messageRes = result.error.toMessageRes(),
                        snackbarType = SnackbarType.ERROR
                    )
                )
            }
        }
    }

    private fun saveNote() {
        val state = uiState.value
        val editor = state.noteEditor
        val noteType = editor.noteType ?: return
        if (!state.permissionLevel.canManagePetCare || !editor.canSave) return

        val updatedPet = when (noteType) {
            PetNoteType.HEALTH -> state.pet.copy(healthNote = editor.normalizedValue)
            PetNoteType.FOOD -> state.pet.copy(foodNote = editor.normalizedValue)
        }

        _uiState.update { currentState ->
            currentState.copy(noteEditor = currentState.noteEditor.beginSaving())
        }

        viewModelScope.launch {
            when (val result = petRepository.savePet(updatedPet)) {
                is AppResult.Success -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            pet = updatedPet,
                            noteEditor = currentState.noteEditor.cancel()
                        )
                    }
                    _eventChannel.send(
                        PetProfileEvent.ShowSnackbar(
                            messageRes = when (noteType) {
                                PetNoteType.HEALTH -> R.string.health_note_saved
                                PetNoteType.FOOD -> R.string.food_note_saved
                            },
                            snackbarType = SnackbarType.SUCCESS,
                            withDismissAction = false
                        )
                    )
                }
                is AppResult.Failure -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            noteEditor = currentState.noteEditor.saveFailed(
                                result.error.toMessageRes()
                            )
                        )
                    }
                }
            }
        }
    }

    private fun onDeleteFoodClick(food: Food) {
        val state = uiState.value
        if (!state.permissionLevel.canManagePetCare || food.petId != state.pet.id) return

        viewModelScope.launch {
            when (val result = foodRepository.deleteFood(food)) {
                is AppResult.Success -> _uiState.update { currentState ->
                    currentState.copy(
                        food = currentState.food.filterNot { it.id == food.id }
                    )
                }
                is AppResult.Failure -> _eventChannel.send(
                    PetProfileEvent.ShowSnackbar(
                        messageRes = result.error.toMessageRes(),
                        snackbarType = SnackbarType.ERROR
                    )
                )
            }
        }
    }

    private fun getPetData(petId: String) {
        _uiState.update { state ->
            state.copy(
                isLoading = true,
                loadErrorMessageRes = null
            )
        }

        viewModelScope.launch {
            val permissionResult = getPetPermission(petId)
            val permissionLevel = when (permissionResult) {
                is AppResult.Success -> permissionResult.data
                is AppResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            loadErrorMessageRes = if (
                                permissionResult.error == FirestoreError.PermissionDenied
                            ) {
                                R.string.pet_profile_access_denied
                            } else {
                                permissionResult.error.toMessageRes()
                            }
                        )
                    }
                    return@launch
                }
            }

            val response = petRepository.getPetById(petId)

            when (response) {
                is AppResult.Success -> {
                    val pet = response.data ?: run {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                loadErrorMessageRes = R.string.pet_not_found_error
                            )
                        }
                        return@launch
                    }

                    val dob = getPetDob(pet)
                    val age = getPetAge(pet, context)
                    val medications = when (
                        val result = medicationRepository.getMedications(petId)
                    ) {
                        is AppResult.Success -> result.data
                        is AppResult.Failure -> {
                            showLoadError(result.error.toMessageRes())
                            return@launch
                        }
                    }
                    val food = when (val result = foodRepository.getAllFood(petId)) {
                        is AppResult.Success -> result.data
                        is AppResult.Failure -> {
                            showLoadError(result.error.toMessageRes())
                            return@launch
                        }
                    }

                    _uiState.update { state ->
                        state.copy(
                            pet = pet,
                            dob = dob,
                            age = age,
                            medications = medications,
                            food = food,
                            permissionLevel = permissionLevel,
                            noteEditor = NoteEditorState(),
                            loadErrorMessageRes = null,
                            isLoading = false
                        )
                    }
                }
                is AppResult.Failure -> _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        loadErrorMessageRes = response.error.toMessageRes()
                    )
                }
            }

        }
    }

    private fun showLoadError(messageRes: Int) {
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                loadErrorMessageRes = messageRes
            )
        }
    }

    fun getPetDob(pet: Pet): String? {
        val year = pet.dobYear ?: return null
        val month = pet.dobMonth
        val day = pet.dobDay

        return runCatching {
            when {
                month == null -> year.toString()
                day == null -> "${Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())} $year"
                else -> LocalDate.of(year, month, day)
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault()))
            }
        }.getOrElse {
            context.getString(R.string.unknown)
        }
    }

    fun getPetAge(pet: Pet, context: Context): String? {
        val year = pet.dobYear ?: return null
        val month = pet.dobMonth
        val day = pet.dobDay

        val today = LocalDate.now()
        val petBirthDate = try {
            LocalDate.of(year, month ?: 1, day ?: 1)
        } catch (_: DateTimeException) {
            return context.getString(R.string.unknown)
        }

        if (petBirthDate.isAfter(today)) {
            return context.getString(R.string.unknown)
        }

        val period = Period.between(petBirthDate, today)
        val years = period.years
        val months = period.months
        val days = period.days

        return when {
            day != null -> {
                when {
                    years >= 1 -> context.resources.getQuantityString(R.plurals.years_old_plural, years, years)
                    months >= 1 -> context.resources.getQuantityString(R.plurals.months_old_plural, months, months)
                    days >= 0 -> context.resources.getQuantityString(R.plurals.days_old_plural, days, days)
                    else -> context.getString(R.string.just_born)
                }
            }
            month != null -> {
                when {
                    years >= 1 -> context.resources.getQuantityString(R.plurals.years_old_plural, years, years)
                    else -> context.resources.getQuantityString(R.plurals.months_old_plural, months, months)
                }
            }
            else -> {
                when {
                    years >= 1 -> context.resources.getQuantityString(R.plurals.years_old_plural, years, years)
                    else -> context.getString(R.string.less_than_a_year_old)
                }
            }
        }
    }

    private fun deletePet(petId: String) {
        val state = uiState.value
        if (!state.permissionLevel.canDeletePet || petId != state.pet.id) return

        viewModelScope.launch {
            when (val result = petRepository.deletePet(petId)) {
                is AppResult.Success -> _eventChannel.send(PetProfileEvent.PetDeleted)
                is AppResult.Failure -> _eventChannel.send(
                    PetProfileEvent.ShowSnackbar(
                        messageRes = result.error.toMessageRes(),
                        snackbarType = SnackbarType.ERROR
                    )
                )
            }
        }
    }
}
