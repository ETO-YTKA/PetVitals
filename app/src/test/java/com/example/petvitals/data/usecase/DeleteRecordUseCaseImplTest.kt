package com.example.petvitals.data.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.Record
import com.example.petvitals.domain.repository.RecordRepository
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteRecordUseCaseImplTest {

    @Test
    fun invoke_rejectsViewerWithoutDeletingRecord() = runTest {
        val repository = FakeRecordRepository()
        val useCase = DeleteRecordUseCaseImpl(
            recordRepository = repository,
            getPetPermissionUseCase = FakeGetPetPermissionUseCase(
                roles = mapOf(
                    "pet-owner" to PermissionLevel.OWNER,
                    "pet-viewer" to PermissionLevel.VIEWER
                )
            )
        )
        val record = Record(
            id = "record-1",
            petIds = listOf("pet-owner", "pet-viewer")
        )

        val result = useCase(record)

        assertEquals(FirestoreError.PermissionDenied, (result as AppResult.Failure).error)
        assertEquals(0, repository.deleteCalls)
    }

    @Test
    fun invoke_deletesAfterEveryLinkedPetGrantsManageAccess() = runTest {
        val repository = FakeRecordRepository()
        val useCase = DeleteRecordUseCaseImpl(
            recordRepository = repository,
            getPetPermissionUseCase = FakeGetPetPermissionUseCase(
                roles = mapOf(
                    "pet-owner" to PermissionLevel.OWNER,
                    "pet-editor" to PermissionLevel.EDITOR
                )
            )
        )
        val record = Record(
            id = "record-1",
            petIds = listOf("pet-owner", "pet-editor")
        )

        val result = useCase(record)

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, repository.deleteCalls)
    }

    private class FakeGetPetPermissionUseCase(
        private val roles: Map<String, PermissionLevel>
    ) : GetPetPermissionUseCase {
        override suspend fun invoke(petId: String): AppResult<FirestoreError, PermissionLevel> =
            roles[petId]
                ?.let { AppResult.Success(it) }
                ?: AppResult.Failure(FirestoreError.PermissionDenied)
    }

    private class FakeRecordRepository : RecordRepository {
        var deleteCalls = 0

        override suspend fun saveRecord(record: Record, previousPetIds: List<String>) =
            AppResult.Success(Unit)

        override suspend fun getCurrentUserRecords(petIds: List<String>) =
            AppResult.Success(emptyList<Record>())

        override suspend fun deleteRecord(record: Record): AppResult<FirestoreError, Unit> {
            deleteCalls++
            return AppResult.Success(Unit)
        }
    }
}
