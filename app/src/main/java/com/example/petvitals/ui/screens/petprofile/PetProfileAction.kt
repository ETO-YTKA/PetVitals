package com.example.petvitals.ui.screens.petprofile

import com.example.petvitals.domain.models.Food
import com.example.petvitals.domain.models.Medication

sealed interface PetProfileAction {
    data class LoadPet(val petId: String) : PetProfileAction
    data object ToggleDeleteModal : PetProfileAction
    data class EditNote(val noteType: PetNoteType) : PetProfileAction
    data object CancelNoteEdit : PetProfileAction
    data class OnNoteChange(val value: String) : PetProfileAction
    data object SaveNote : PetProfileAction
    data class DeletePet(val petId: String) : PetProfileAction
    data class DeleteMedication(val medication: Medication) : PetProfileAction
    data class DeleteFood(val food: Food) : PetProfileAction
}
