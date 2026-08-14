package com.example.petvitals.ui.screens.managerecord

import com.example.petvitals.domain.models.RecordType

sealed interface ManageRecordAction {
    data class OnTitleChange(val title: String) : ManageRecordAction
    data class OnTypeChange(val type: RecordType) : ManageRecordAction
    data object OnDatePickerToggle : ManageRecordAction
    data object OnTimePickerToggle : ManageRecordAction
    data class OnDateChange(val date: Long?) : ManageRecordAction
    data class OnTimeChange(val hour: Int, val minute: Int) : ManageRecordAction
    data class OnDescriptionChange(val description: String) : ManageRecordAction
    data object OnPetSelectorToggle : ManageRecordAction
    data class OnPetToggle(val petId: String) : ManageRecordAction
    data object OnRetryLoad : ManageRecordAction
    data object OnReloadLatest : ManageRecordAction
    data class OnSave(
        val fallbackTitle: String,
        val onSuccess: () -> Unit
    ) : ManageRecordAction
}