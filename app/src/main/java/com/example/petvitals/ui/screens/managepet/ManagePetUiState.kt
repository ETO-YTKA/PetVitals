package com.example.petvitals.ui.screens.managepet

import android.net.Uri
import com.example.petvitals.domain.models.Gender
import com.example.petvitals.domain.models.PetSpecies
import com.example.petvitals.ui.components.DropDownOption
import com.example.petvitals.ui.components.PopUpState

data class ManagePetUiState(
    val isLoading: Boolean = false,

    val petId: String? = null,
    val name: String = "",
    val selectedSpecies: PetSpecies? = null,
    val selectedGender: Gender? = null,
    val breed: String = "",

    val avatarUri: Uri? = null, //picked image from gallery
    val avatarByteArray: ByteArray? = null, //stored in firestore

    val dobString: String = "",
    val selectedDobMonth: Int? = null,
    val dobYear: String = "",
    val dobMillis: Long? = null,

    val showDatePicker: Boolean = false,
    val isDobApprox: Boolean = false,

    val monthOptions: List<DropDownOption<Int?>> = emptyList(),
    val speciesOptions: List<DropDownOption<PetSpecies?>> = emptyList(),
    val genderOptions: List<DropDownOption<Gender?>> = emptyList(),

    val nameErrorMessage: String? = null,
    val breedErrorMessage: String? = null,
    val dobErrorMessage: String? = null,
    val dobYearErrorMessage: String? = null,
    val speciesErrorMessage: String? = null,

    val popUpState: PopUpState<ManagePetAction>? = null,
)
