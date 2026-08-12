package com.example.petvitals.ui.screens.records

import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.Record
import com.example.petvitals.domain.models.RecordOverview
import com.example.petvitals.domain.models.RecordType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class RecordsListMapperTest {

    @Test
    fun mapRecordEntries_filtersNullableDescriptionBySearchPetAndType() {
        val pet = Pet(id = "pet-1", name = "Mochi")
        val matchingRecord = Record(
            id = "record-1",
            title = "Annual vaccination",
            type = RecordType.VACCINATION,
            eventDate = Date(2_000L),
            description = null,
            petIds = listOf(pet.id)
        )
        val nonMatchingRecord = Record(
            id = "record-2",
            title = "Dental visit",
            type = RecordType.VET_VISIT,
            eventDate = Date(1_000L),
            petIds = listOf("pet-2")
        )

        val entries = mapRecordEntries(
            records = listOf(
                RecordOverview(matchingRecord, listOf(pet), canManage = true),
                RecordOverview(nonMatchingRecord, emptyList(), canManage = false)
            ),
            query = "vaccination",
            selectedPetIds = setOf(pet.id),
            selectedTypes = setOf(RecordType.VACCINATION)
        )

        assertEquals(2, entries.size)
        assertEquals(matchingRecord.id, (entries[1] as RecordsListEntry.RecordItem).overview.record.id)
    }
}
