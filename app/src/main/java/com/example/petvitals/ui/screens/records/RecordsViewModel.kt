package com.example.petvitals.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.data.repository.record.Record
import com.example.petvitals.data.repository.record.RecordRepository
import com.example.petvitals.data.service.account.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class RecordsUiState(
    val records: List<Record> = emptyList(),
    val isRefreshing: Boolean = false,
    val selectedRecords: List<Record> = emptyList(),
    val selectionMode: Boolean = false
)

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val accountService: AccountService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshRecords()
    }

    fun refreshRecords() {
        _uiState.update { state -> state.copy(isRefreshing = true) }
        viewModelScope.launch {
            val records = recordRepository.getAllRecord(accountService.currentUserId)
            _uiState.update { state ->
                state.copy(
                    records = records,
                    isRefreshing = false,
                    selectionMode = false,
                    selectedRecords = emptyList()
                )
            }
        }
    }

    fun deleteRecord(record: Record) {
        viewModelScope.launch {
            recordRepository.deleteRecord(record)
            refreshRecords()
        }
        _uiState.update { state ->
            state.copy(
                selectionMode = false,
                selectedRecords = emptyList()
            )
        }
    }

    fun selectRecord(record: Record) {
        _uiState.update { state ->
            val selectedRecords = state.selectedRecords.toMutableList()

            if (selectedRecords.contains(record)) {
                selectedRecords.remove(record)
            } else {
                selectedRecords.add(record)
            }

            val newSelectionMode = selectedRecords.isNotEmpty()

            state.copy(
                selectedRecords = selectedRecords,
                selectionMode = newSelectionMode
            )
        }
    }

    fun formatDateForDisplay(millis: Long): String {
        return SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(millis))
    }
}