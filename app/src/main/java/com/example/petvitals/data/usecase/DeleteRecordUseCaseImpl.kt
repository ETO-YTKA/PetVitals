package com.example.petvitals.data.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Record
import com.example.petvitals.domain.models.canManagePetCare
import com.example.petvitals.domain.repository.RecordRepository
import com.example.petvitals.domain.usecase.DeleteRecordUseCase
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import javax.inject.Inject

class DeleteRecordUseCaseImpl @Inject constructor(
    private val recordRepository: RecordRepository,
    private val getPetPermissionUseCase: GetPetPermissionUseCase
) : DeleteRecordUseCase {

    override suspend fun invoke(record: Record): AppResult<FirestoreError, Unit> {
        val petIds = record.petIds.distinct()
        if (petIds.isEmpty()) return AppResult.Failure(FirestoreError.PermissionDenied)

        petIds.forEach { petId ->
            when (val permissionResult = getPetPermissionUseCase(petId)) {
                is AppResult.Failure -> return permissionResult
                is AppResult.Success -> if (!permissionResult.data.canManagePetCare) {
                    return AppResult.Failure(FirestoreError.PermissionDenied)
                }
            }
        }

        return recordRepository.deleteRecord(record)
    }
}
