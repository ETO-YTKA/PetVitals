package com.example.petvitals.ui.screens.records

import com.example.petvitals.domain.models.RecordOverview
import com.example.petvitals.domain.models.RecordType
import com.example.petvitals.ui.utils.formatDateToStringLocale
import java.util.Date

sealed interface RecordsListEntry {
    data class Header(val date: Date) : RecordsListEntry
    data class RecordItem(val overview: RecordOverview) : RecordsListEntry
}

internal fun mapRecordEntries(
    records: List<RecordOverview>,
    query: String,
    selectedPetIds: Set<String>,
    selectedTypes: Set<RecordType>
): List<RecordsListEntry> {
    val filteredRecords = records
        .asSequence()
        .filter { overview ->
            overview.record.title.contains(query, ignoreCase = true) ||
                overview.record.description.orEmpty().contains(query, ignoreCase = true)
        }
        .filter { overview ->
            selectedPetIds.isEmpty() || overview.pets.any { it.id in selectedPetIds }
        }
        .filter { overview ->
            selectedTypes.isEmpty() || overview.record.type in selectedTypes
        }
        .sortedByDescending { overview ->
            overview.record.eventDate ?: overview.record.createdAt
        }
        .toList()

    if (filteredRecords.isEmpty()) return emptyList()

    return buildList {
        filteredRecords
            .groupBy { overview ->
                formatDateToStringLocale(
                    overview.record.eventDate ?: overview.record.createdAt,
                    DATE_GROUP_PATTERN
                )
            }
            .values
            .forEach { recordsForDay ->
                val firstRecord = recordsForDay.first()
                add(
                    RecordsListEntry.Header(
                        firstRecord.record.eventDate ?: firstRecord.record.createdAt
                    )
                )
                recordsForDay.forEach { add(RecordsListEntry.RecordItem(it)) }
            }
    }
}

private const val DATE_GROUP_PATTERN = "yyyy-MM-dd"
