package com.example.petvitals.ui.screens.records

import com.example.petvitals.domain.models.RecordType

sealed interface RecordsAction {
    data class OnSearchQueryChange(val query: String) : RecordsAction
    data object OnClearSearch : RecordsAction
    data object OnClearFilters : RecordsAction
    data object OnRefresh : RecordsAction
    data class OnPetFilterToggle(val petId: String) : RecordsAction
    data class OnTypeFilterToggle(val type: RecordType) : RecordsAction
    data class OnRecordExpansionToggle(val recordId: String) : RecordsAction
    data class OnDeleteRecordClick(val recordId: String) : RecordsAction
}
