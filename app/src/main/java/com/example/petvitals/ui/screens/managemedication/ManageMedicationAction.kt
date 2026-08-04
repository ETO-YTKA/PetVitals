package com.example.petvitals.ui.screens.managemedication

sealed interface ManageMedicationAction {
    data class OnNameChange(val name: String) : ManageMedicationAction
    data class OnDosageChange(val dosage: String) : ManageMedicationAction
    data class OnFrequencyChange(val frequency: String) : ManageMedicationAction
    data class OnRegularChange(val isRegular: Boolean) : ManageMedicationAction
    data object OnStartDatePickerToggle : ManageMedicationAction
    data object OnEndDatePickerToggle : ManageMedicationAction
    data class OnStartDateChange(val date: Long?) : ManageMedicationAction
    data class OnEndDateChange(val date: Long?) : ManageMedicationAction
    data class OnNoteChange(val note: String) : ManageMedicationAction
    data object OnRetryLoad : ManageMedicationAction
    data class OnSave(val onSuccess: () -> Unit) : ManageMedicationAction
}
