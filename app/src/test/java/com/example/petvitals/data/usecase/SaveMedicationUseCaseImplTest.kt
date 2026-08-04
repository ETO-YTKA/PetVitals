package com.example.petvitals.data.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Medication
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.repository.MedicationRepository
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveMedicationUseCaseImplTest {

    @Test
    fun invoke_asEditor_savesMedicationAndReturnsSuccess() = runTest {
        val repository = FakeMedicationRepository(AppResult.Success(Unit))
        val useCase = createUseCase(
            permissionResult = AppResult.Success(PermissionLevel.EDITOR),
            repository = repository
        )

        val result = useCase(MEDICATION)

        assertTrue(result is AppResult.Success<*>)
        assertEquals(1, repository.saveCalls)
        assertEquals(MEDICATION, repository.savedMedication)
    }

    @Test
    fun invoke_asViewer_returnsPermissionDeniedWithoutSaving() = runTest {
        val repository = FakeMedicationRepository(AppResult.Success(Unit))
        val useCase = createUseCase(
            permissionResult = AppResult.Success(PermissionLevel.VIEWER),
            repository = repository
        )

        val result = useCase(MEDICATION)

        assertEquals(FirestoreError.PermissionDenied, (result as AppResult.Failure).error)
        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun invoke_whenPermissionLookupFails_forwardsFailureWithoutSaving() = runTest {
        val repository = FakeMedicationRepository(AppResult.Success(Unit))
        val useCase = createUseCase(
            permissionResult = AppResult.Failure(FirestoreError.Network),
            repository = repository
        )

        val result = useCase(MEDICATION)

        assertEquals(FirestoreError.Network, (result as AppResult.Failure).error)
        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun invoke_whenRepositoryFails_forwardsFailure() = runTest {
        val repository = FakeMedicationRepository(
            AppResult.Failure(FirestoreError.Unknown)
        )
        val useCase = createUseCase(
            permissionResult = AppResult.Success(PermissionLevel.OWNER),
            repository = repository
        )

        val result = useCase(MEDICATION)

        assertEquals(FirestoreError.Unknown, (result as AppResult.Failure).error)
        assertEquals(1, repository.saveCalls)
    }

    @Test
    fun invoke_rechecksPermissionForEverySave() = runTest {
        val repository = FakeMedicationRepository(AppResult.Success(Unit))
        val permissions = SequencedGetPetPermissionUseCase(
            ArrayDeque(
                listOf(
                    AppResult.Success(PermissionLevel.EDITOR),
                    AppResult.Success(PermissionLevel.VIEWER)
                )
            )
        )
        val useCase = SaveMedicationUseCaseImpl(repository, permissions)

        val firstResult = useCase(MEDICATION)
        val secondResult = useCase(MEDICATION)

        assertTrue(firstResult is AppResult.Success<*>)
        assertEquals(
            FirestoreError.PermissionDenied,
            (secondResult as AppResult.Failure).error
        )
        assertEquals(2, permissions.calls)
        assertEquals(1, repository.saveCalls)
    }

    private fun createUseCase(
        permissionResult: AppResult<FirestoreError, PermissionLevel>,
        repository: MedicationRepository
    ) = SaveMedicationUseCaseImpl(
        medicationRepository = repository,
        getPetPermissionUseCase = FakeGetPetPermissionUseCase(permissionResult)
    )

    private class FakeGetPetPermissionUseCase(
        private val result: AppResult<FirestoreError, PermissionLevel>
    ) : GetPetPermissionUseCase {
        override suspend fun invoke(
            petId: String
        ): AppResult<FirestoreError, PermissionLevel> = result
    }

    private class SequencedGetPetPermissionUseCase(
        private val results: ArrayDeque<AppResult<FirestoreError, PermissionLevel>>
    ) : GetPetPermissionUseCase {
        var calls = 0

        override suspend fun invoke(
            petId: String
        ): AppResult<FirestoreError, PermissionLevel> {
            calls++
            return results.removeFirst()
        }
    }

    private class FakeMedicationRepository(
        private val saveResult: AppResult<FirestoreError, Unit>
    ) : MedicationRepository {
        var saveCalls = 0
        var savedMedication: Medication? = null

        override suspend fun getMedications(
            petId: String
        ): AppResult<FirestoreError, List<Medication>> = AppResult.Success(emptyList())

        override suspend fun saveMedication(
            medication: Medication
        ): AppResult<FirestoreError, Unit> {
            saveCalls++
            savedMedication = medication
            return saveResult
        }

        override suspend fun deleteMedication(
            medication: Medication
        ): AppResult<FirestoreError, Unit> = AppResult.Success(Unit)

        override suspend fun getMedicationById(
            petId: String,
            medicationId: String
        ): AppResult<FirestoreError, Medication?> = AppResult.Success(null)
    }

    private companion object {
        val MEDICATION = Medication(
            id = "medication-id",
            petId = "pet-id",
            name = "Antibiotic",
            dosage = "1 tablet",
            frequency = "Twice daily"
        )
    }
}
