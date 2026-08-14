package com.example.petvitals.data.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.Record
import com.example.petvitals.domain.repository.RecordRepository
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveRecordUseCaseImplTest {

    @Test
    fun invoke_checksEveryPreviousAndCurrentPetBeforeSaving() = runTest {
        val repository = FakeRecordRepository()
        val permissions = FakeGetPetPermissionUseCase(
            mapOf(
                "pet-old" to AppResult.Success(PermissionLevel.OWNER),
                "pet-kept" to AppResult.Success(PermissionLevel.EDITOR),
                "pet-new" to AppResult.Success(PermissionLevel.OWNER)
            )
        )
        val useCase = SaveRecordUseCaseImpl(repository, permissions)
        val record = Record(petIds = listOf("pet-kept", "pet-new", "pet-new"))

        val result = useCase(record, previousPetIds = listOf("pet-old", "pet-kept"))

        assertTrue(result is AppResult.Success<*>)
        assertEquals(setOf("pet-old", "pet-kept", "pet-new"), permissions.requested.toSet())
        assertEquals(3, permissions.requested.size)
        assertEquals(1, repository.saveCalls)
        assertEquals(listOf("pet-old", "pet-kept"), repository.previousPetIds)
    }

    @Test
    fun invoke_whenAnyPetIsViewer_deniesWithoutSaving() = runTest {
        val repository = FakeRecordRepository()
        val permissions = FakeGetPetPermissionUseCase(
            mapOf(
                "pet-owner" to AppResult.Success(PermissionLevel.OWNER),
                "pet-viewer" to AppResult.Success(PermissionLevel.VIEWER)
            )
        )
        val useCase = SaveRecordUseCaseImpl(repository, permissions)

        val result = useCase(
            Record(petIds = listOf("pet-owner", "pet-viewer")),
            previousPetIds = emptyList()
        )

        assertEquals(FirestoreError.PermissionDenied, (result as AppResult.Failure).error)
        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun invoke_whenPermissionLookupFails_forwardsFailureWithoutSaving() = runTest {
        val repository = FakeRecordRepository()
        val permissions = FakeGetPetPermissionUseCase(
            mapOf("pet-id" to AppResult.Failure(FirestoreError.Network))
        )
        val useCase = SaveRecordUseCaseImpl(repository, permissions)

        val result = useCase(Record(petIds = listOf("pet-id")), emptyList())

        assertEquals(FirestoreError.Network, (result as AppResult.Failure).error)
        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun invoke_whenRepositoryFails_forwardsFailure() = runTest {
        val repository = FakeRecordRepository(
            result = AppResult.Failure(FirestoreError.Unknown)
        )
        val permissions = FakeGetPetPermissionUseCase(
            mapOf("pet-id" to AppResult.Success(PermissionLevel.EDITOR))
        )
        val useCase = SaveRecordUseCaseImpl(repository, permissions)

        val result = useCase(Record(petIds = listOf("pet-id")), emptyList())

        assertEquals(FirestoreError.Unknown, (result as AppResult.Failure).error)
        assertEquals(1, repository.saveCalls)
    }

    @Test
    fun invoke_withoutAnyPet_deniesWithoutSaving() = runTest {
        val repository = FakeRecordRepository()
        val useCase = SaveRecordUseCaseImpl(repository, FakeGetPetPermissionUseCase(emptyMap()))

        val result = useCase(Record(petIds = emptyList()), emptyList())

        assertEquals(FirestoreError.PermissionDenied, (result as AppResult.Failure).error)
        assertEquals(0, repository.saveCalls)
    }

    private class FakeGetPetPermissionUseCase(
        private val results: Map<String, AppResult<FirestoreError, PermissionLevel>>
    ) : GetPetPermissionUseCase {
        val requested = mutableListOf<String>()

        override suspend fun invoke(petId: String): AppResult<FirestoreError, PermissionLevel> {
            requested += petId
            return results[petId] ?: AppResult.Failure(FirestoreError.PermissionDenied)
        }
    }

    private class FakeRecordRepository(
        private val result: AppResult<FirestoreError, Unit> = AppResult.Success(Unit)
    ) : RecordRepository {
        var saveCalls = 0
        var previousPetIds: List<String>? = null

        override suspend fun saveRecord(
            record: Record,
            previousPetIds: List<String>
        ): AppResult<FirestoreError, Unit> {
            saveCalls++
            this.previousPetIds = previousPetIds
            return result
        }

        override suspend fun getCurrentUserRecords(petIds: List<String>) =
            AppResult.Success(emptyList<Record>())

        override suspend fun deleteRecord(record: Record) = AppResult.Success(Unit)
    }
}
