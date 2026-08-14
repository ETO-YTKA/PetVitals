package com.example.petvitals.data.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Record
import com.example.petvitals.domain.models.canManagePetCare
import com.example.petvitals.domain.repository.RecordRepository
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import com.example.petvitals.domain.usecase.SaveRecordUseCase
import javax.inject.Inject

class SaveRecordUseCaseImpl @Inject constructor(
    private val recordRepository: RecordRepository,
    private val getPetPermissionUseCase: GetPetPermissionUseCase
) : SaveRecordUseCase {

    override suspend fun invoke(
        record: Record,
        previousPetIds: List<String>
    ): AppResult<FirestoreError, Unit> {
        val petIds = (previousPetIds + record.petIds)
            .filter(String::isNotBlank)
            .distinct()
        if (record.petIds.none(String::isNotBlank)) {
            return AppResult.Failure(FirestoreError.PermissionDenied)
        }

        petIds.forEach { petId ->
            when (val permissionResult = getPetPermissionUseCase(petId)) {
                is AppResult.Failure -> return permissionResult
                is AppResult.Success -> if (!permissionResult.data.canManagePetCare) {
                    return AppResult.Failure(FirestoreError.PermissionDenied)
                }
            }
        }

        return recordRepository.saveRecord(record, previousPetIds)
    }
}