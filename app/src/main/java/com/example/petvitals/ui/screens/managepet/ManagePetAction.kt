package com.example.petvitals.ui.screens.managepet

import android.net.Uri
import com.example.petvitals.domain.models.Gender
import com.example.petvitals.domain.models.PetSpecies

sealed interface ManagePetAction {
    data class OnNameChange(val name: String) : ManagePetAction
    data class OnSpeciesChange(val species: PetSpecies?) : ManagePetAction
    data class OnGenderChange(val gender: Gender?) : ManagePetAction
    data class OnBreedChange(val breed: String) : ManagePetAction
    data class OnIsDobApproxChange(val isDobApprox: Boolean) : ManagePetAction
    data class OnShowDatePickerChange(val show: Boolean) : ManagePetAction
    data class OnDobMillisChange(val dobMillis: Long?) : ManagePetAction
    data class OnDobMonthChange(val month: Int?) : ManagePetAction
    data class OnDobYearChange(val year: String) : ManagePetAction
    data class OnImageUriChange(val uri: Uri?) : ManagePetAction
    data class SavePet(val petId: String?, val onSuccess: () -> Unit) : ManagePetAction
}