package com.example.petvitals.ui.screens.managemedication

import androidx.annotation.StringRes

data class ManageMedicationUiState(
    val isLoading: Boolean = false,
    @param:StringRes val loadErrorMessageRes: Int? = null,

    val petId: String = "",
    val medicationId: String? = null,

    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val isRegular: Boolean = false,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val note: String = "",

    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,

    @param:StringRes val nameErrorMessageRes: Int? = null,
    @param:StringRes val dosageErrorMessageRes: Int? = null,
    @param:StringRes val frequencyErrorMessageRes: Int? = null,
    @param:StringRes val scheduleErrorMessageRes: Int? = null,
    @param:StringRes val noteErrorMessageRes: Int? = null
)