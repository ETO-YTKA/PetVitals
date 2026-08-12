package com.example.petvitals.data.repository

import com.example.petvitals.domain.models.Record
import org.junit.Assert.assertEquals
import org.junit.Test

class RecordRepositoryImplTest {

    @Test
    fun mergeRecordCopies_preservesEveryPetPathFromDivergentCopies() {
        val copies = listOf(
            Record(id = "record-1", title = "Updated", petIds = listOf("pet-a")),
            Record(id = "record-1", title = "Original", petIds = listOf("pet-a", "pet-b"))
        )

        val merged = mergeRecordCopies(copies)

        assertEquals(1, merged.size)
        assertEquals(setOf("pet-a", "pet-b"), merged.single().petIds.toSet())
    }

    @Test
    fun createRecordWritePlan_deletesRemovedPetsAndWritesCurrentPets() {
        val plan = createRecordWritePlan(
            previousPetIds = listOf("pet-a", "pet-b"),
            currentPetIds = listOf("pet-b", "pet-c", "pet-c")
        )

        assertEquals(setOf("pet-a"), plan.petIdsToDelete)
        assertEquals(setOf("pet-b", "pet-c"), plan.petIdsToSet)
    }

    @Test
    fun mergeRecordCopies_usesContentFromHighestRevision() {
        val copies = listOf(
            Record(id = "record-1", title = "Original", revision = 1),
            Record(id = "record-1", title = "Updated", revision = 2)
        )

        val merged = mergeRecordCopies(copies)

        assertEquals("Updated", merged.single().title)
        assertEquals(2, merged.single().revision)
    }
}
