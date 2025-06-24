package com.example.petvitals.ui.screens.add_pet

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet.DobPrecision
import com.example.petvitals.data.repository.pet.Gender
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetRepository
import com.example.petvitals.data.repository.pet.PetSpecies
import com.example.petvitals.data.repository.pet_permission.PermissionLevel
import com.example.petvitals.data.repository.pet_permission.PetPermission
import com.example.petvitals.data.repository.pet_permission.PetPermissionRepository
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.ui.components.DropDownOption
import com.example.petvitals.utils.decodeBase64ToImage
import com.example.petvitals.utils.processImageUri
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
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class AddEditPetUiState(
    val isLoading: Boolean = false,

    val name: String = "",
    val selectedSpecies: PetSpecies = PetSpecies.CAT,
    val selectedGender: Gender = Gender.MALE,
    val breed: String = "",
    val dobString: String = "",
    val selectedDobMonth: Int? = null,
    val dobYear: String = "",
    val avatarUri: Uri? = null,
    val avatarByteArray: ByteArray? = null,

    val dobMillis: Long? = null,

    val showModal: Boolean = false,
    val editMode: Boolean = false,
    val isDobApprox: Boolean = false,

    val monthOptions: List<DropDownOption<Int?>> = emptyList(),
    val speciesOptions: List<DropDownOption<PetSpecies>> = emptyList(),
    val genderOptions: List<DropDownOption<Gender>> = emptyList(),

    val nameErrorMessage: String? = null,
    val breedErrorMessage: String? = null,
    val dobErrorMessage: String? = null,
    val dobYearErrorMessage: String? = null,

    val isNameError: Boolean = false,
    val isBreedError: Boolean = false,
    val isDobError: Boolean = false,
    val isDobYearError: Boolean = false
)

@HiltViewModel
class AddEditPetViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val accountService: AccountService,
    private val petPermissionRepository: PetPermissionRepository,
    @ApplicationContext private val context: Context
): ViewModel() {
    private val _uiState = MutableStateFlow(AddEditPetUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { state ->
            state.copy(
                monthOptions = populateMonthOptions(),
                speciesOptions = populateSpeciesOptions(),
                genderOptions = populateGenderOptions(),
                dobString = context.getString(R.string.tap_to_select_date)
            )
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { state ->
            state.copy(name = name)
        }
    }

    fun onSpeciesChange(species: PetSpecies) {
        _uiState.update { state ->
            state.copy(selectedSpecies = species)
        }
    }

    fun onGenderChange(gender: Gender) {
        _uiState.update { state ->
            state.copy(selectedGender = gender)
        }
    }

    fun onBreedChange(breed: String) {
        _uiState.update { state ->
            state.copy(breed = breed)
        }
    }

    fun onDobApproxChange(isApproximate: Boolean) {
        _uiState.update { state ->
            state.copy(isDobApprox = isApproximate)
        }
    }

    fun onShowModalChange(show: Boolean) {
        _uiState.update { state ->
            state.copy(showModal = show)
        }
    }

    fun onDobMillisChange(dobMillis: Long?) {
        _uiState.update { state ->
            state.copy(
                dobMillis = dobMillis,
                dobString = millisToDobString(dobMillis)
            )
        }
    }

    fun onDobMonthChange(month: Int?) {
        _uiState.update { state ->
            state.copy(selectedDobMonth = month)
        }
    }

    fun onDobYearChange(year: String) {
        if (!year.contains(Regex("[^0-9]"))) {

            _uiState.update { state ->
                state.copy(dobYear = year)
            }
        }
    }

    fun onImageUriChange(uri: Uri?) {
        _uiState.update { state ->
            state.copy(avatarUri = uri)
        }
    }

    fun populateSpeciesOptions(): List<DropDownOption<PetSpecies>> {
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

    fun populateMonthOptions(): List<DropDownOption<Int?>> {
        return listOf(
            DropDownOption(
                display = context.getString(R.string.unknown),
                value = null
            ),
            DropDownOption(
                display = context.getString(R.string.january),
                value = 0
            ),
            DropDownOption(
                display = context.getString(R.string.february),
                value = 1
            ),
            DropDownOption(
                display = context.getString(R.string.march),
                value = 2
            ),
            DropDownOption(
                display = context.getString(R.string.april),
                value = 3
            ),
            DropDownOption(
                display = context.getString(R.string.may),
                value = 4
            ),
            DropDownOption(
                display = context.getString(R.string.june),
                value = 5
            ),
            DropDownOption(
                display = context.getString(R.string.july),
                value = 6
            ),
            DropDownOption(
                display = context.getString(R.string.august),
                value = 7
            ),
            DropDownOption(
                display = context.getString(R.string.september),
                value = 8
            ),
            DropDownOption(
                display = context.getString(R.string.october),
                value = 9
            ),
            DropDownOption(
                display = context.getString(R.string.november),
                value = 10
            ),
            DropDownOption(
                display = context.getString(R.string.december),
                value = 11
            )
        )
    }

    fun populateGenderOptions(): List<DropDownOption<Gender>> {
        return listOf(
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

    private fun isFormValid(): Boolean {
        var isValid = true
        _uiState.update {
            it.copy(
                isNameError = false,
                isBreedError = false,
                isDobError = false,
                isDobYearError = false,
                nameErrorMessage = null,
                breedErrorMessage = null,
                dobErrorMessage = null,
                dobYearErrorMessage = null
            )
        }

        if (uiState.value.name.isBlank()) {
            isValid = false
            _uiState.update {
                it.copy(
                    isNameError = true,
                    nameErrorMessage = context.getString(R.string.pet_name_cannot_be_empty)
                )
            }
        } else if (uiState.value.name.length > 50) {
            isValid = false
            _uiState.update {
                it.copy(
                    isNameError = true,
                    nameErrorMessage = context.getString(R.string.pet_name_cannot_be_longer_than_error)
                )
            }
        }

        if (uiState.value.breed.length > 100) {
            isValid = false
            _uiState.update {
                it.copy(
                    isBreedError = true,
                    breedErrorMessage = context.getString(R.string.breed_cannot_be_longer_than_error)
                )
            }
        }

        if (uiState.value.isDobApprox) {
            if (uiState.value.dobYear.isBlank()) {
                isValid = false
                _uiState.update {
                    it.copy(
                        isDobYearError = true,
                        dobYearErrorMessage = context.getString(R.string.year_cannot_be_empty)
                    )
                }
            } else {
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val minYear = 1980
                val dobYearInt = uiState.value.dobYear.toInt()

                if (dobYearInt > currentYear) {
                    _uiState.update {
                        it.copy(
                            isDobYearError = true,
                            dobYearErrorMessage = context.getString(R.string.year_cannot_be_in_the_future)
                        )
                    }
                    isValid = false
                }
                else if (dobYearInt < minYear) {
                    _uiState.update {
                        it.copy(
                            isDobYearError = true,
                            dobYearErrorMessage = context.getString(
                                R.string.year_cannot_be_before,
                                minYear
                            )
                        )
                    }
                    isValid = false
                }
            }
        } else {
            if (uiState.value.dobMillis == null) {
                isValid = false
                _uiState.update {
                    it.copy(
                        isDobError = true,
                        dobErrorMessage = context.getString(R.string.date_of_birth_cannot_be_empty)
                    )
                }
            }
        }

        return isValid
    }

    fun loadPetData(petId: String) {
        _uiState.update { state ->
            state.copy(isLoading = true)
        }

        viewModelScope.launch {
            val pet = petRepository.getPetById(petId)

            pet?.let { pet ->
                val calendar = Calendar.getInstance().apply { timeInMillis = pet.dobMillis }

                val month = when (pet.dobPrecision) {
                    DobPrecision.YEAR -> null
                    else -> calendar.get(Calendar.MONTH)
                }

                _uiState.update { state ->
                    state.copy(
                        name = pet.name,
                        selectedSpecies = pet.species,
                        breed = pet.breed,
                        selectedGender = pet.gender,
                        isDobApprox = pet.dobPrecision.isApproximate,
                        dobMillis = pet.dobMillis,
                        dobString = millisToDobString(pet.dobMillis),
                        selectedDobMonth = month,
                        dobYear = calendar.get(Calendar.YEAR).toString(),
                        editMode = true,
                        avatarByteArray = pet.avatar?.let { decodeBase64ToImage(it) },
                        isLoading = false
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun savePet(
        petId: String?,
        onSuccess: () -> Unit
    ) {
        if (!isFormValid()) return

        val uiState = uiState.value
        val userId = accountService.currentUserId

        val dobMillis = when {
            uiState.isDobApprox -> {
                approxDobToMillis(uiState.selectedDobMonth, uiState.dobYear.toInt())
            }
            else -> uiState.dobMillis ?: 0
        }

        val avatar = when {
            uiState.avatarUri != null -> processImageUri(context, uiState.avatarUri)
            uiState.avatarByteArray != null -> Base64.encode(uiState.avatarByteArray)
            else -> null
        }

        val dobPrecision = when {
            !uiState.isDobApprox -> DobPrecision.EXACT
            uiState.selectedDobMonth != null -> DobPrecision.YEAR_MONTH
            else -> DobPrecision.YEAR
        }

        val basePet = Pet(
            name = uiState.name,
            species = uiState.selectedSpecies,
            breed = uiState.breed,
            gender = uiState.selectedGender,
            dobMillis = dobMillis,
            dobPrecision = dobPrecision,
            avatar = avatar
        )

        val isNewPet = petId == null
        val petToSave = if (isNewPet) basePet else basePet.copy(id = petId)

        viewModelScope.launch {
            try {
                petRepository.savePet(petToSave)

                if (isNewPet) {
                    val petPermission = PetPermission(
                        userId = userId,
                        petId = petToSave.id,
                        permissionLevel = PermissionLevel.OWNER
                    )

                    try {
                        petPermissionRepository.savePetPermission(petPermission)
                    } catch (e: Exception) {
                        Log.e("AddEditPetViewModel", "Error saving UserPet for new pet: ${e.message}", e)
                        return@launch
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                Log.e("AddEditPetViewModel", "Error saving Pet: ${e.message}", e)
            }
        }
    }

    fun approxDobToMillis(month: Int?, year: Int): Long {
        val calendar = Calendar.getInstance()
        val day = 1
        calendar.set(
            year,
            month ?: 1,
            day
        )
        Log.d("AddEditPetViewModel", "birthDateToMillis: ${Date(calendar.timeInMillis)}")
        return calendar.timeInMillis
    }

    fun millisToDobString(millis: Long?): String {
        return millis?.let {
            SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(it))
        } ?: context.getString(R.string.tap_to_select_date)
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