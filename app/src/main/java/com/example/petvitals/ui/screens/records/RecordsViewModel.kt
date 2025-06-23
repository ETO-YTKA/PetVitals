package com.example.petvitals.ui.screens.records

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetRepository
import com.example.petvitals.data.repository.pet_permission.PermissionLevel
import com.example.petvitals.data.repository.pet_permission.PetPermissionRepository
import com.example.petvitals.data.repository.record.Record
import com.example.petvitals.data.repository.record.RecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordsUiState(
    val recordsWithPets: List<RecordWithPets> = emptyList(),
    val isRefreshing: Boolean = false,
    val selectedRecords: List<Record> = emptyList(),
    val selectionMode: Boolean = false,
    val searchQuery: String = ""
)

data class RecordWithPets(
    val record: Record,
    val pets: List<Pet>
)

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val petRepository: PetRepository,
    private val petPermissionRepository: PetPermissionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getRecords()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state -> state.copy(searchQuery = query) }
        getRecords()
    }

    fun getRecords() {
        if (uiState.value.searchQuery.isBlank()) {
            _uiState.update { state -> state.copy(isRefreshing = true) }
        }

        viewModelScope.launch {
            val records = recordRepository.getCurrentUserRecords(uiState.value.searchQuery)
            val recordWithPets = records.map { record ->

                val pets: List<Pet> = record.petIds.mapNotNull { petId ->
                    val pet = petRepository.getPetById(petId)
                    val currentUserPermission = petPermissionRepository.getCurrentUserPermissionLevel(petId) ?: return@mapNotNull null
                    pet?.copy(currentUserPermission = currentUserPermission)
                }
                val minPetPermission = pets.minByOrNull { pet -> pet.currentUserPermission }

                val recordWithPermission = record.copy(currentUserPermission = minPetPermission?.currentUserPermission ?: PermissionLevel.OWNER)

                Log.d("RecordsViewModel", "record ${recordWithPermission.currentUserPermission}: ${minPetPermission?.currentUserPermission}")
                RecordWithPets(recordWithPermission, pets)
            }

            _uiState.update { state ->
                state.copy(
                    recordsWithPets = recordWithPets,
                    isRefreshing = false,
                    selectionMode = false,
                    selectedRecords = emptyList()
                )
            }
        }
    }

    fun deleteSelectedRecords() {
        viewModelScope.launch {
            val selectedRecords = uiState.value.selectedRecords
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

    fun deleteRecord(record: Record) {
        viewModelScope.launch {
            recordRepository.deleteRecord(record)
            getRecords()
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
}