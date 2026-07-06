package com.example.petvitals.ui.screens.managepet

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.error.PetDataError
import com.example.petvitals.domain.models.Gender
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.PetSpecies
import com.example.petvitals.domain.repository.PetRepository
import com.example.petvitals.domain.usecase.CreatePetUseCase
import com.example.petvitals.domain.validator.PetDataValidator
import com.example.petvitals.ui.components.DropDownOption
import com.example.petvitals.ui.components.PopUpButton
import com.example.petvitals.ui.components.PopUpState
import com.example.petvitals.ui.components.PopUpType
import com.example.petvitals.ui.utils.debounceValidation
import com.example.petvitals.ui.utils.decodeBase64ToImage
import com.example.petvitals.ui.utils.processImageUri
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val MAX_DAY_LENGTH = 2
private const val MAX_YEAR_LENGTH = 4

@HiltViewModel
class ManagePetViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val createPetUseCase: CreatePetUseCase,
    private val petDataValidator: PetDataValidator,
    @ApplicationContext private val context: Context
): ViewModel() {
    private val _uiState = MutableStateFlow(ManagePetUiState())
    val uiState = _uiState.asStateFlow()

    private var nameValidationJob: Job? = null
    private var breedValidationJob: Job? = null
    private var dobValidationJob: Job? = null
    private var speciesValidationJob: Job? = null
    private var pendingSaveOnSuccess: (() -> Unit)? = null

    init {
        _uiState.update { state ->
            state.copy(
                monthOptions = populateMonthOptions(),
                speciesOptions = populateSpeciesOptions(),
                genderOptions = populateGenderOptions()
            )
        }
    }

    fun onAction(action: ManagePetAction) {
        when(action) {
            is ManagePetAction.OnBreedChange -> onBreedChange(action.breed)
            is ManagePetAction.OnDobDayChange -> onDobDayChange(action.day)
            is ManagePetAction.OnDobMonthChange -> onDobMonthChange(action.month)
            is ManagePetAction.OnDobYearChange -> onDobYearChange(action.year)
            is ManagePetAction.OnGenderChange -> onGenderChange(action.gender)
            is ManagePetAction.OnImageUriChange -> onImageUriChange(action.uri)
            is ManagePetAction.OnNameChange -> onNameChange(action.name)
            is ManagePetAction.OnSpeciesChange -> onSpeciesChange(action.species)
            is ManagePetAction.SavePet -> savePet(action.petId, action.onSuccess)
            ManagePetAction.RetrySavePet -> retrySavePet()
            ManagePetAction.DismissPopUp -> dismissPopUp(clearPendingSave = true)
        }
    }

    private fun onNameChange(name: String) {
        _uiState.update { state ->
            state.copy(name = name)
        }

        nameValidationJob = debounceValidation(
            scope = viewModelScope,
            previousJob = nameValidationJob,
            validate = { validateName(name) }
        )
    }

    private fun onSpeciesChange(species: PetSpecies?) {
        _uiState.update { state ->
            state.copy(
                selectedSpecies = species,
                speciesErrorMessage = null
            )
        }

        speciesValidationJob = debounceValidation(
            scope = viewModelScope,
            previousJob = speciesValidationJob,
            validate = { validateSpecies(species) }
        )
    }

    private fun onGenderChange(gender: Gender?) {
        _uiState.update { state ->
            state.copy(selectedGender = gender)
        }
    }

    private fun onBreedChange(breed: String) {
        _uiState.update { state ->
            state.copy(breed = breed)
        }

        breedValidationJob = debounceValidation(
            scope = viewModelScope,
            previousJob = breedValidationJob,
            validate = { validateBreed(breed) }
        )
    }

    private fun onDobMonthChange(month: Int?) {
        _uiState.update { state ->
            state.copy(selectedDobMonth = month)
        }

        dobValidationJob = debounceValidation(
            scope = viewModelScope,
            previousJob = dobValidationJob,
            validate = { validateDobParts(uiState.value.dobYear, month, uiState.value.dobDay) }
        )
    }

    private fun onDobDayChange(day: String) {
        val acceptedDay = if (!day.contains(Regex("[^0-9]")) && day.length <= MAX_DAY_LENGTH) {

            _uiState.update { state ->
                state.copy(dobDay = day)
            }
            day
        } else {
            uiState.value.dobDay
        }

        dobValidationJob = debounceValidation(
            scope = viewModelScope,
            previousJob = dobValidationJob,
            validate = { validateDobParts(uiState.value.dobYear, uiState.value.selectedDobMonth, acceptedDay) }
        )
    }

    private fun onDobYearChange(year: String) {
        val acceptedYear = if (!year.contains(Regex("[^0-9]")) && year.length <= MAX_YEAR_LENGTH) {

            _uiState.update { state ->
                state.copy(dobYear = year)
            }
            year
        } else {
            uiState.value.dobYear
        }

        dobValidationJob = debounceValidation(
            scope = viewModelScope,
            previousJob = dobValidationJob,
            validate = { validateDobParts(acceptedYear, uiState.value.selectedDobMonth, uiState.value.dobDay) }
        )
    }

    private fun onImageUriChange(uri: Uri?) {
        _uiState.update { state ->
            state.copy(avatarUri = uri)
        }
    }

    private fun populateSpeciesOptions(): List<DropDownOption<PetSpecies?>> {
        return listOf(
            DropDownOption(
                display = context.getString(R.string.cat),
                value = PetSpecies.CAT
            ),
            DropDownOption(
                display = context.getString(R.string.dog),
                value = PetSpecies.DOG
            ),
        )
    }

    private fun populateMonthOptions(): List<DropDownOption<Int?>> {
        return listOf(
            DropDownOption(
                display = "",
                value = null
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
            )
        )
    }

    private fun populateGenderOptions(): List<DropDownOption<Gender?>> {
        return listOf(
            DropDownOption(
                display = "",
                value = null
            ),
            DropDownOption(
                display = context.getString(R.string.male),
                value = Gender.MALE
            ),
            DropDownOption(
                display = context.getString(R.string.female),
                value = Gender.FEMALE
            )
        )
    }

    fun loadPetData(petId: String) {
        _uiState.update { state ->
            state.copy(isLoading = true)
        }

        viewModelScope.launch {
            val response = petRepository.getPetById(petId)

            when (response) {
                is AppResult.Success -> {
                    val pet = response.data

                    pet?.let {
                        _uiState.update { state ->
                            state.copy(
                                petId = pet.id,
                                name = pet.name,
                                selectedSpecies = pet.species,
                                breed = pet.breed.orEmpty(),
                                selectedGender = pet.gender,
                                selectedDobMonth = pet.dobMonth,
                                dobYear = pet.dobYear?.toString().orEmpty(),
                                dobDay = pet.dobDay?.toString().orEmpty(),
                                avatarByteArray = pet.avatar?.let { decodeBase64ToImage(it) },
                                isLoading = false
                            )
                        }
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun savePet(
        petId: String?,
        onSuccess: () -> Unit
    ) {
        if (isFormValid()) {
            val uiState = uiState.value
            pendingSaveOnSuccess = onSuccess

            val dobYear = uiState.dobYear.trim().toIntOrNull()
            val dobMonth = uiState.selectedDobMonth
            val dobDay = uiState.dobDay.trim().toIntOrNull()

            val avatar = when {
                uiState.avatarUri != null -> processImageUri(context, uiState.avatarUri)
                uiState.avatarByteArray != null -> Base64.encode(uiState.avatarByteArray)
                else -> null
            }

            val basePet = Pet(
                name = uiState.name,
                species = uiState.selectedSpecies!!,
                breed = uiState.breed,
                gender = uiState.selectedGender,
                dobYear = dobYear,
                dobMonth = dobMonth,
                dobDay = dobDay,
                avatar = avatar
            )

            if (petId == null) {
                viewModelScope.launch {
                    when (val result = createPetUseCase.invoke(pet = basePet)) {
                        is AppResult.Success -> {
                            pendingSaveOnSuccess = null
                            dismissPopUp()
                            onSuccess()
                        }
                        is AppResult.Failure -> {
                            showSaveFailurePopUp(result.error)
                        }
                    }
                }
            } else {
                //TODO Update pet
            }
        }
    }

    private fun retrySavePet() {
        val onSuccess = pendingSaveOnSuccess ?: return
        dismissPopUp()
        savePet(uiState.value.petId, onSuccess)
    }

    private fun dismissPopUp(clearPendingSave: Boolean = false) {
        if (clearPendingSave) {
            pendingSaveOnSuccess = null
        }

        _uiState.update { state ->
            state.copy(popUpState = null)
        }
    }

    private fun showSaveFailurePopUp(error: FirestoreError) {
        val popUpState: PopUpState<ManagePetAction> = when (error) {
            FirestoreError.Network -> PopUpState(
                type = PopUpType.WARNING,
                title = context.getString(R.string.manage_pet_save_network_error_title),
                message = context.getString(R.string.manage_pet_save_network_error_message),
                primaryButton = PopUpButton(
                    text = context.getString(R.string.try_again),
                    action = ManagePetAction.RetrySavePet,
                    dismissAfterClick = false
                ),
                secondaryButton = PopUpButton(
                    text = context.getString(R.string.keep_editing),
                    action = ManagePetAction.DismissPopUp
                )
            )
            FirestoreError.PermissionDenied,
            FirestoreError.Unauthenticated,
            FirestoreError.Unknown -> PopUpState(
                type = PopUpType.ALERT,
                title = context.getString(R.string.manage_pet_save_error_title),
                message = context.getString(R.string.manage_pet_save_error_message),
                primaryButton = PopUpButton(
                    text = context.getString(R.string.got_it),
                    action = ManagePetAction.DismissPopUp
                )
            )
        }

        _uiState.update { state ->
            state.copy(popUpState = popUpState)
        }
    }

    private fun isFormValid(): Boolean {
        val state = uiState.value

        val nameError = petDataValidator.validateName(state.name).messageOrNull()
        val breedError = petDataValidator.validateBreed(state.breed).messageOrNull()
        val speciesError = petDataValidator.validateSpecies(state.selectedSpecies).messageOrNull()

        val dobValidationResult = petDataValidator.validateDobParts(
            year = state.dobYear,
            month = state.selectedDobMonth,
            day = state.dobDay
        )
        val dobError = dobValidationResult.messageOrNull()

        _uiState.update {
            it.copy(
                nameErrorMessage = nameError,
                breedErrorMessage = breedError,
                dobErrorMessage = dobError,
                speciesErrorMessage = speciesError
            )
        }

        return listOf(
            nameError,
            breedError,
            dobError,
            speciesError
        ).all { it == null }
    }

    private fun validateName(name: String) {
        when (val result = petDataValidator.validateName(name)) {
            is AppResult.Success -> {
                _uiState.update { state ->
                    state.copy(nameErrorMessage = null)
                }
            }
            is AppResult.Failure -> {
                _uiState.update {
                    it.copy(nameErrorMessage = result.error.toMessage())
                }
            }
        }
    }

    private fun validateBreed(breed: String) {
        when (val result = petDataValidator.validateBreed(breed)) {
            is AppResult.Success -> {
                _uiState.update { state ->
                    state.copy(breedErrorMessage = null)
                }
            }
            is AppResult.Failure -> {
                _uiState.update {
                    it.copy(breedErrorMessage = result.error.toMessage())
                }
            }
        }
    }

    private fun validateDobParts(year: String, month: Int?, day: String) {
        when (val result = petDataValidator.validateDobParts(year, month, day)) {
            is AppResult.Success -> {
                _uiState.update { state ->
                    state.copy(dobErrorMessage = null)
                }
            }
            is AppResult.Failure -> {
                _uiState.update {
                    it.copy(dobErrorMessage = result.error.toMessage())
                }
            }
        }
    }

    private fun validateSpecies(species: PetSpecies?) {
        when (val result = petDataValidator.validateSpecies(species)) {
            is AppResult.Success -> {
                _uiState.update { state ->
                    state.copy(speciesErrorMessage = null)
                }
            }
            is AppResult.Failure -> {
                _uiState.update {
                    it.copy(speciesErrorMessage = result.error.toMessage())
                }
            }
        }
    }

    private fun PetDataError.toMessage(): String {
        return when (this) {
            PetDataError.EMPTY_NAME -> context.getString(R.string.pet_name_cannot_be_empty)
            PetDataError.NAME_TOO_LONG -> context.getString(R.string.pet_name_cannot_be_longer_than_error)
            PetDataError.BREED_TOO_LONG -> context.getString(R.string.breed_cannot_be_longer_than_error)
            PetDataError.EMPTY_DOB -> context.getString(R.string.date_of_birth_cannot_be_empty)
            PetDataError.EMPTY_DOB_YEAR -> context.getString(R.string.year_cannot_be_empty)
            PetDataError.INVALID_DOB_YEAR -> context.getString(R.string.year_must_be_number)
            PetDataError.INVALID_DOB_DAY -> context.getString(R.string.day_must_match_month)
            PetDataError.DOB_YEAR_IN_FUTURE -> context.getString(R.string.year_cannot_be_in_the_future)
            PetDataError.EMPTY_SPECIES -> context.getString(R.string.species_cannot_be_empty)
        }
    }

    private fun AppResult<PetDataError, Unit>.messageOrNull(): String? {
        return when (this) {
            is AppResult.Success -> null
            is AppResult.Failure -> error.toMessage()
        }
    }
}