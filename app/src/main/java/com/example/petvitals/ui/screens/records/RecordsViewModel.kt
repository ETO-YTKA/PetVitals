package com.example.petvitals.ui.screens.records

import android.content.Context
import android.icu.util.Calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetRepository
import com.example.petvitals.data.repository.pet_permission.PermissionLevel
import com.example.petvitals.data.repository.pet_permission.PetPermissionRepository
import com.example.petvitals.data.repository.record.Record
import com.example.petvitals.data.repository.record.RecordRepository
import com.example.petvitals.data.repository.record.RecordType
import com.example.petvitals.utils.formatDateToStringLocale
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RecordsListEntry {
    data class Header(val dateString: String) : RecordsListEntry()
    data class RecordItem(val recordWithPets: RecordWithPets) : RecordsListEntry()
}

data class RecordWithPets(
    val record: Record,
    val pets: List<Pet>
)

data class RecordsUiState(
    val displayedRecords: List<RecordsListEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedRecords: List<Record> = emptyList(),
    val selectedPetFilters: Set<String> = emptySet(),
    val selectedTypeFilters: Set<RecordType> = emptySet(),

    val rawRecords: List<RecordWithPets> = emptyList(),

    val allPetsForFiltering: List<Pet> = emptyList(),
    val allRecordTypesForFiltering: List<RecordType> = RecordType.entries,

    val isRefreshing: Boolean = false,
    val selectionMode: Boolean = false,
)

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val petRepository: PetRepository,
    private val petPermissionRepository: PetPermissionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedPetFilters = MutableStateFlow(emptySet<String>())
    private val _selectedTypeFilters = MutableStateFlow(emptySet<RecordType>())
    private val _allRecordsRaw = MutableStateFlow(emptyList<RecordWithPets>())

    init {
        getAllPetsForFiltering()
        getRecords()

        viewModelScope.launch {
            combine(
                _allRecordsRaw,
                _searchQuery,
                _selectedPetFilters,
                _selectedTypeFilters
            ) { allRecords, query, petFilters, typeFilters ->
                filterAndGroupRecords(allRecords, query, petFilters, typeFilters)
            }.collect { groupedList ->
                _uiState.update { it.copy(displayedRecords = groupedList) }
            }
        }
    }

    fun getRecords() {
        _uiState.update { state -> state.copy(isRefreshing = true) }

        viewModelScope.launch {
            val records = recordRepository.getCurrentUserRecords()

            val recordWithPetsList = records.map { record ->

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

            _allRecordsRaw.value = recordWithPetsList
            _uiState.update { state ->
                state.copy(
                    rawRecords = recordWithPetsList,
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

    fun getAllPetsForFiltering() {
        viewModelScope.launch {
            val pets = petPermissionRepository.getCurrentUserPets().mapNotNull { petPermission ->
                petRepository.getPetById(petPermission.petId)
            }
            _uiState.update { state -> state.copy(allPetsForFiltering = pets) }
        }
    }

    private fun filterAndGroupRecords(
        allRecords: List<RecordWithPets>,
        query: String,
        petFilters: Set<String>,
        typeFilters: Set<RecordType>
    ): List<RecordsListEntry> {

        val filteredList = allRecords
            .filter { recordWithPets ->
                recordWithPets.record.title.contains(query, ignoreCase = true)
                        || recordWithPets.record.description.contains(query, ignoreCase = true)
            }
            .filter { recordWithPets ->
                val petIds = recordWithPets.record.petIds
                if (petFilters.isEmpty()) true else petIds.any { petId -> petFilters.contains(petId) }
            }
            .filter { recordWithPets ->
                val recordType = recordWithPets.record.type
                if (typeFilters.isEmpty()) true else typeFilters.contains(recordType)
            }

        return groupAndFlattenRecords(filteredList)
    }

    private fun groupAndFlattenRecords(records: List<RecordWithPets>): List<RecordsListEntry> {
        if (records.isEmpty()) return emptyList()

        val sortedRecords = records.sortedByDescending { it.record.date }

        // Group records by their day (yyyy-MM-dd string)
        val datePatternForKeys = "yyyy-MM-dd"
        val groupedMap = sortedRecords
            .groupBy { formatDateToStringLocale(it.record.date, datePatternForKeys) }

        val flattenedList = mutableListOf<RecordsListEntry>()
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        val todayKey = formatDateToStringLocale(today.time, datePatternForKeys)
        val yesterdayKey = formatDateToStringLocale(yesterday.time, datePatternForKeys)

        // Iterate through the grouped map to create headers and record items
        groupedMap.forEach { (dateKey, recordsInDay) ->

            val headerText = when (dateKey) {
                todayKey -> context.getString(R.string.today)
                yesterdayKey -> context.getString(R.string.yesterday)
                else -> {
                    val displayDate = recordsInDay.first().record.date
                    val displayDateCalendar = Calendar.getInstance().apply { time = displayDate }
                    when {
                        today[Calendar.YEAR] == displayDateCalendar[Calendar.YEAR] -> formatDateToStringLocale(displayDate, "EEEE, MMMM d")
                        else -> formatDateToStringLocale(displayDate, "EEEE, MMMM d, yyyy")
                    }
                }
            }

            flattenedList.add(RecordsListEntry.Header(headerText))

            recordsInDay.forEach { record ->
                flattenedList.add(RecordsListEntry.RecordItem(record))
            }
        }
        return flattenedList
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { state -> state.copy(searchQuery = query) }
    }

    fun onPetFilterChipClick(petId: String) {
        _selectedPetFilters.update { currentFilters ->
            if (currentFilters.contains(petId)) {
                currentFilters - petId
            } else {
                currentFilters + petId
            }
        }

        _uiState.update { state -> state.copy(selectedPetFilters = _selectedPetFilters.value) }
    }

    fun onTypeFilterChipClicked(recordType: RecordType) {
        _selectedTypeFilters.update { currentFilters ->
            if (currentFilters.contains(recordType)) {
                currentFilters - recordType
            } else {
                currentFilters + recordType
            }
        }
        _uiState.update { it.copy(selectedTypeFilters = _selectedTypeFilters.value) }
    }
}