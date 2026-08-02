package com.example.petvitals.ui.screens.managefood

sealed interface ManageFoodAction {
    data class OnNameChange(val name: String) : ManageFoodAction
    data class OnPortionChange(val portion: String) : ManageFoodAction
    data class OnFrequencyChange(val frequency: String) : ManageFoodAction
    data class OnNoteChange(val note: String) : ManageFoodAction
    data class OnSave(val onSuccess: () -> Unit) : ManageFoodAction
}
