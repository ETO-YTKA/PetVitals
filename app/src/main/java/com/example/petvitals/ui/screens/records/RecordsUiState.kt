package com.example.petvitals.ui.screens.records

import androidx.annotation.StringRes
import com.example.petvitals.domain.models.RecordOverview
import com.example.petvitals.domain.models.RecordType

data class RecordsUiState(
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
    val records: List<RecordOverview> = emptyList(),
    val searchQuery: String = "",
    val selectedPetIds: Set<String> = emptySet(),
    val selectedTypeFilters: Set<RecordType> = emptySet(),
    val expandedRecordIds: Set<String> = emptySet(),
    val deletingRecordId: String? = null
)