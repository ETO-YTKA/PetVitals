package com.example.petvitals.ui.screens.create_record

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.repository.record.Record
import com.example.petvitals.data.repository.record.RecordRepository
import com.example.petvitals.data.repository.record.RecordType
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.ui.components.DropDownOption
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class CreateRecordUiState(
    val title: String = "",
    val selectedType: RecordType = RecordType.NOTE,
    val typeOptions: List<DropDownOption<RecordType>> = emptyList(),
    val date: Long = Calendar.getInstance().timeInMillis,
    val showModal: Boolean = false,
    val description: String = "",
)

@HiltViewModel
class CreateRecordViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val accountService: AccountService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRecordUiState())
    val uiState = _uiState.asStateFlow()

    init {
        populateTypeOptions()
    }

    fun onTitleChange(title: String) {
        _uiState.update { state ->
            state.copy(title = title)
        }
    }

    fun onTypeChange(type: RecordType) {
        _uiState.update { state ->
            state.copy(selectedType = type)
        }
    }

    fun onShowModalChange(showModal: Boolean) {
        _uiState.update { state ->
            state.copy(showModal = showModal)
        }

    }

    fun onDateChange(date: Long?) {
        _uiState.update { state ->
            state.copy(date = date ?: Calendar.getInstance().timeInMillis)
        }

    }

    fun onDescriptionChange(description: String) {
        _uiState.update { state ->
            state.copy(description = description)
        }
    }

    fun createRecord() {
        val userId = accountService.currentUserId
        val title = if (uiState.value.title.isBlank()) {
            context.getString(uiState.value.selectedType.titleResId)
        } else {
            uiState.value.title
        }

        val record = Record(
            userId = userId,
            title = title,
            type = uiState.value.selectedType,
            date = uiState.value.date,
            description = uiState.value.description
        )

        viewModelScope.launch {
            recordRepository.createUserRecord(record)
        }
    }

    fun populateTypeOptions() {
        _uiState.update { state ->
            state.copy(
                typeOptions = listOf(
                    DropDownOption(
                        display = context.getString(RecordType.NOTE.titleResId),
                        value = RecordType.NOTE
                    ),
                    DropDownOption(
                        display = context.getString(RecordType.VACCINATION.titleResId),
                        value = RecordType.VACCINATION
                    ),
                    DropDownOption(
                        display = context.getString(RecordType.MEDICATION.titleResId),
                        value = RecordType.MEDICATION
                    ),
                    DropDownOption(
                        display = context.getString(RecordType.VET_VISIT.titleResId),
                        value = RecordType.VET_VISIT
                    ),
                    DropDownOption(
                        display = context.getString(RecordType.SYMPTOM.titleResId),
                        value = RecordType.SYMPTOM
                    ),
                    DropDownOption(
                        display = context.getString(RecordType.GROOMING.titleResId),
                        value = RecordType.GROOMING
                    ),
                    DropDownOption(
                        display = context.getString(RecordType.INCIDENT.titleResId),
                        value = RecordType.INCIDENT
                    )
                )
            )
        }
    }

    fun formatDateForDisplay(millis: Long?, context: Context): String {
        return millis?.let {
            SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(it))
        } ?: context.getString(R.string.tap_to_select_date)
    }
}