package com.example.petvitals.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetRepository
import com.example.petvitals.data.repository.pet_permission.PermissionLevel
import com.example.petvitals.data.repository.pet_permission.PetPermissionRepository
import com.example.petvitals.data.repository.record.Record
import com.example.petvitals.data.repository.record.RecordRepository
import com.example.petvitals.data.repository.record.RecordType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordsUiState(
    val recordsWithPets: List<RecordWithPets> = emptyList(),
    val filteredRecordsWithPets: List<RecordWithPets>? = null,
    val searchQuery: String = "",
    val selectedRecords: List<Record> = emptyList(),
    val selectedPetFilters: Set<String> = emptySet(),
    val selectedTypeFilters: Set<RecordType> = emptySet(),

    val allPetsForFiltering: List<Pet> = emptyList(),
    val allRecordTypesForFiltering: List<RecordType> = RecordType.entries,

    val isRefreshing: Boolean = false,
    val selectionMode: Boolean = false,
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
        getAllPetsForFiltering()
        getRecords()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state -> state.copy(searchQuery = query) }

        search()
    }

    fun getRecords() {
        _uiState.update { state -> state.copy(isRefreshing = true) }

        viewModelScope.launch {
            val records = recordRepository.getCurrentUserRecords()

            val recordWithPets = records.map { record ->

                val pets: List<Pet> = record.petIds.mapNotNull { petId ->
                    val pet = petRepository.getPetById(petId)
                    val currentUserPermission = petPermissionRepository.getCurrentUserPermissionLevel(petId)
                        ?: return@mapNotNull null

                    pet?.copy(currentUserPermission = currentUserPermission)
                }
                val minPetPermission = pets.maxByOrNull { pet -> pet.currentUserPermission }
                val recordWithPermission = record.copy(currentUserPermission = minPetPermission?.currentUserPermission ?: PermissionLevel.OWNER)

                RecordWithPets(recordWithPermission, pets)
            }

            _uiState.update { state ->
                state.copy(
                    recordsWithPets = recordWithPets,
                    isRefreshing = false,
                    selectionMode = false,
                    selectedRecords = emptyList(),
                    filteredRecordsWithPets = null
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

    fun getAllPetsForFiltering() {
        viewModelScope.launch {
            val pets = petPermissionRepository.getCurrentUserPets().mapNotNull { petPermission ->
                petRepository.getPetById(petPermission.petId)
            }
            _uiState.update { state -> state.copy(allPetsForFiltering = pets) }
        }
    }

    fun onPetFilterChipClick(petId: String) {
        _uiState.update { state ->
            val currentFilters = state.selectedPetFilters.toMutableSet()
            if (currentFilters.contains(petId)) {
                currentFilters.remove(petId)
            } else {
                currentFilters.add(petId)
            }
            state.copy(selectedPetFilters = currentFilters)
        }

        search()
    }

    fun onTypeFilterChipClicked(recordType: RecordType) {
        _uiState.update { currentState ->
            val currentFilters = currentState.selectedTypeFilters.toMutableSet()
            if (currentFilters.contains(recordType)) {
                currentFilters.remove(recordType)
            } else {
                currentFilters.add(recordType)
            }
            currentState.copy(selectedTypeFilters = currentFilters)
        }

        search()
    }


    fun search() {
        val filteredRecords = uiState.value.recordsWithPets
            .filter { recordWithPets ->
                val query = uiState.value.searchQuery

                recordWithPets.record.title.contains(query, ignoreCase = true)
                    || recordWithPets.record.description.contains(query, ignoreCase = true)
            }
            .filter { recordWithPets ->
                val petIds = recordWithPets.record.petIds
                val petFilter = uiState.value.selectedPetFilters

                if (petFilter.isEmpty()) return@filter true
                petIds.any { petId ->
                    petFilter.contains(petId)
                }
            }
            .filter { recordWithPets ->
                val recordType = recordWithPets.record.type
                val typeFilter = uiState.value.selectedTypeFilters

                if (typeFilter.isEmpty()) return@filter true
                typeFilter.contains(recordType)
            }

        _uiState.update { state -> state.copy(filteredRecordsWithPets = filteredRecords) }
    }
}