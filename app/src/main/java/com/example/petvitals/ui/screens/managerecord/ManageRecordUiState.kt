package com.example.petvitals.ui.screens.managerecord

import androidx.annotation.StringRes
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.RecordType

data class ManageRecordUiState(
    val isLoading: Boolean = false,
    @param:StringRes val loadErrorMessageRes: Int? = null,

    val routeRecordId: String? = null,
    val draftRecordId: String = "",
    val title: String = "",
    val selectedType: RecordType = RecordType.NOTE,
    val eventDate: Long = 0L,
    val description: String = "",
    val selectedPetIds: Set<String> = emptySet(),

    val originalPetIds: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val revision: Long = 0L,
    val availablePets: List<Pet> = emptyList(),

    val showPetSelector: Boolean = false,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val isSaving: Boolean = false,
    val hasConflict: Boolean = false,

    @param:StringRes val titleErrorMessageRes: Int? = null,
    @param:StringRes val descriptionErrorMessageRes: Int? = null,
    @param:StringRes val petSelectionErrorMessageRes: Int? = null
)
