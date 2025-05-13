package com.example.petvitals.ui.screens.records

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.data.repository.record.Record
import com.example.petvitals.data.repository.record.RecordRepository
import com.example.petvitals.data.service.account.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class RecordsUiState(
    val records: List<Record> = emptyList(),
    val isRefreshing: Boolean = false
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
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        viewModelScope.launch {
            val records = recordRepository.getAllRecord(accountService.currentUserId)
            _uiState.value = _uiState.value.copy(records = records, isRefreshing = false)
        }
    }

    fun formatDateForDisplay(millis: Long, context: Context): String {
        return SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(millis))
    }
}