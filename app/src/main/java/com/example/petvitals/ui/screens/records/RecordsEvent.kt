package com.example.petvitals.ui.screens.records

import androidx.annotation.StringRes

sealed interface RecordsEvent {
    data class OnShowError(@StringRes val messageRes: Int) : RecordsEvent
}
