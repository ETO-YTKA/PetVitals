package com.example.petvitals.ui.screens.managefood

data class ManageFoodUiState(
    val isLoading: Boolean = false,

    val petId: String = "",
    val foodId: String? = null,
    val name: String = "",
    val portion: String = "",
    val frequency: String = "",
    val note: String = "",

    val nameErrorMessageRes: Int? = null,
    val portionErrorMessageRes: Int? = null,
    val frequencyErrorMessageRes: Int? = null,
    val noteErrorMessageRes: Int? = null,
    val saveErrorMessageRes: Int? = null
)