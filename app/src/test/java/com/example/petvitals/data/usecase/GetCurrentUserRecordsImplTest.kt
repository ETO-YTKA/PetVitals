package com.example.petvitals.data.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.Record
import com.example.petvitals.domain.repository.PetRepository
import com.example.petvitals.domain.repository.RecordRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCurrentUserRecordsImplTest {

    @Test
    fun invoke_joinsAccessiblePetsAndRequiresManageAccessToEveryLinkedPet() = runTest {
        val ownerPet = Pet(
            id = "pet-owner",
            name = "Mochi",
            currentUserPermission = PermissionLevel.OWNER
        )
        val editorPet = Pet(
            id = "pet-editor",
            name = "Pepper",
            currentUserPermission = PermissionLevel.EDITOR
        )
        val viewerPet = Pet(
            id = "pet-viewer",
            name = "Nori",
            currentUserPermission = PermissionLevel.VIEWER
        )
        val manageableRecord = Record(
            id = "record-manageable",
            petIds = listOf(ownerPet.id, editorPet.id)
        )
        val viewerRecord = Record(
            id = "record-viewer",
            petIds = listOf(ownerPet.id, viewerPet.id)
        )
        val partiallyAccessibleRecord = Record(
            id = "record-partial",
            petIds = listOf(ownerPet.id, "pet-inaccessible")
        )
        val useCase = GetCurrentUserRecordsImpl(
            recordRepository = FakeRecordRepository(
                records = listOf(manageableRecord, viewerRecord, partiallyAccessibleRecord)
            ),
            petRepository = FakePetRepository(
                pets = listOf(ownerPet, editorPet, viewerPet)
            )
        )

        val result = useCase()

        val overviews = (result as AppResult.Success).data.associateBy { it.record.id }
        assertEquals(listOf(ownerPet, editorPet), overviews.getValue(manageableRecord.id).pets)
        assertTrue(overviews.getValue(manageableRecord.id).canManage)
        assertFalse(overviews.getValue(viewerRecord.id).canManage)
        assertFalse(overviews.getValue(partiallyAccessibleRecord.id).canManage)
    }

    private class FakeRecordRepository(
        private val records: List<Record>
    ) : RecordRepository {
        override suspend fun saveRecord(record: Record, previousPetIds: List<String>) =
            AppResult.Success(Unit)

        override suspend fun getCurrentUserRecords(petIds: List<String>) =
            AppResult.Success(records)

        override suspend fun deleteRecord(record: Record) = AppResult.Success(Unit)
    }

    private class FakePetRepository(
        private val pets: List<Pet>
    ) : PetRepository {
        override suspend fun savePet(pet: Pet) = AppResult.Success(Unit)

        override suspend fun updatePet(pet: Pet) = AppResult.Success(Unit)

        override suspend fun getPetById(petId: String) = AppResult.Success<Pet?>(null)

        override suspend fun getCurrentUserPets() = AppResult.Success(pets)

        override suspend fun deletePet(petId: String) = AppResult.Success(Unit)

        override suspend fun createPetWithOwner(pet: Pet, member: Member) =
            AppResult.Success(Unit)
    }
}
