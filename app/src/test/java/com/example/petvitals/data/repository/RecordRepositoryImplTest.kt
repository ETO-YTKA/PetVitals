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
    fun collectKnownPreviousPetIds_preservesClaimedAndStoredPaths() {
        val petIds = collectKnownPreviousPetIds(
            claimedPetIds = listOf("pet-a", "pet-b"),
            storedRecords = listOf(
                Record(petIds = listOf("pet-a")),
                Record(petIds = listOf("pet-b", "pet-c"))
            )
        )

        assertEquals(setOf("pet-a", "pet-b", "pet-c"), petIds)
    }

    @Test
    fun recordCopiesMatchRevision_rejectsAnyDivergentRevision() {
        assertEquals(
            false,
            recordCopiesMatchRevision(
                storedRecords = listOf(Record(revision = 2), Record(revision = 3)),
                expectedRevision = 3
            )
        )
        assertEquals(
            true,
            recordCopiesMatchRevision(
                storedRecords = listOf(Record(revision = 3), Record(revision = 3)),
                expectedRevision = 3
            )
        )
    }

    @Test
    fun classifyRecordCreate_identifiesNewIdempotentAndConflictingCreates() {
        val incoming = Record(
            id = "record-1",
            title = "Checkup",
            petIds = listOf("pet-a", "pet-b"),
            createdAt = java.util.Date(1_000L),
            revision = 0
        )
        val persisted = incoming.copy(revision = 1)

        assertEquals(
            RecordCreateState.NEW,
            classifyRecordCreate(incoming, emptyMap())
        )
        assertEquals(
            RecordCreateState.IDEMPOTENT_RETRY,
            classifyRecordCreate(
                incoming,
                mapOf(
                    "pet-a" to persisted.copy(id = "deserialized-a"),
                    "pet-b" to persisted.copy(id = "deserialized-b")
                )
            )
        )
        assertEquals(
            RecordCreateState.CONFLICT,
            classifyRecordCreate(incoming, mapOf("pet-a" to persisted))
        )
        assertEquals(
            RecordCreateState.CONFLICT,
            classifyRecordCreate(
                incoming,
                mapOf(
                    "pet-a" to persisted,
                    "pet-b" to persisted.copy(title = "Different")
                )
            )
        )
    }

    @Test
    fun recordCopiesMatchIdentity_requiresIncomingCreationTimeToMatchEveryCopy() {
        val stored = listOf(
            Record(createdAt = java.util.Date(1_000L)),
            Record(createdAt = java.util.Date(1_000L))
        )

        assertEquals(
            true,
            recordCopiesMatchIdentity(stored, incomingCreatedAt = java.util.Date(1_000L))
        )
        assertEquals(
            false,
            recordCopiesMatchIdentity(stored, incomingCreatedAt = java.util.Date(2_000L))
        )
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
