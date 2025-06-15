package com.example.petvitals.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetRepository
import com.example.petvitals.data.repository.record.Record
import com.example.petvitals.data.repository.record.RecordRepository
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
    val recordWithPets: List<RecordWithPets> = emptyList(),
    val isRefreshing: Boolean = false,
    val selectedRecords: List<Record> = emptyList(),
    val selectionMode: Boolean = false,
    val searchCond: String = ""
)

data class RecordWithPets(
    val record: Record,
    val pets: List<Pet>
)

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getRecords()
    }

    fun onSearchCondChange(cond: String) {
        _uiState.update { state -> state.copy(searchCond = cond) }
        search()
    }

    fun getRecords() {
        _uiState.update { state -> state.copy(isRefreshing = true) }
        viewModelScope.launch {
            val records = recordRepository.getAllRecord()
            val recordWithPets = records.map { record ->
                val pets: List<Pet> = record.petsId.mapNotNull { petId ->
                    petRepository.getPetById(petId)
                }
                RecordWithPets(record, pets)
            }

            _uiState.update { state ->
                state.copy(
                    recordWithPets = recordWithPets,
                    isRefreshing = false,
                    selectionMode = false,
                    selectedRecords = emptyList()
                )
            }
        }
    }

    fun deleteSelectedRecords() {
        viewModelScope.launch {
            val selectedRecords = _uiState.value.selectedRecords
            selectedRecords.forEach { record ->
                recordRepository.deleteRecord(record)
            }
            getRecords()
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

    fun formatDateForDisplay(date: Date): String {
        return SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault()).format(date)
    }

    fun search() {
        viewModelScope.launch {
            val records = recordRepository.getRecordsByCondition(uiState.value.searchCond)
            val recordWithPets = records.map { record ->
                val pets: List<Pet> = record.petsId.mapNotNull { petId ->
                    petRepository.getPetById(petId)
                }
                RecordWithPets(record, pets)
            }

            _uiState.update { state -> state.copy(recordWithPets = recordWithPets) }
        }
    }
}