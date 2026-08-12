package com.example.petvitals.data.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.RecordOverview
import com.example.petvitals.domain.models.canManagePetCare
import com.example.petvitals.domain.repository.PetRepository
import com.example.petvitals.domain.repository.RecordRepository
import com.example.petvitals.domain.usecase.GetCurrentUserRecords
import jakarta.inject.Inject

class GetCurrentUserRecordsImpl @Inject constructor(
    private val recordRepository: RecordRepository,
    private val petRepository: PetRepository
) : GetCurrentUserRecords {
    override suspend fun invoke(): AppResult<FirestoreError, List<RecordOverview>> {

        return when (val petsResult = petRepository.getCurrentUserPets()) {
            is AppResult.Failure -> petsResult
            is AppResult.Success -> {
                val petsById = petsResult.data.associateBy { it.id }

                when (
                    val recordsResult = recordRepository.getCurrentUserRecords(
                        petsResult.data.map { it.id }
                    )
                ) {
                    is AppResult.Failure -> recordsResult
                    is AppResult.Success -> AppResult.Success(
                        recordsResult.data.map { record ->
                            val linkedPetIds = record.petIds.distinct()
                            val linkedPets = linkedPetIds.mapNotNull(petsById::get)

                            RecordOverview(
                                record = record,
                                pets = linkedPets,
                                canManage = linkedPetIds.isNotEmpty() &&
                                    linkedPets.size == linkedPetIds.size &&
                                    linkedPets.all {
                                        it.currentUserPermission.canManagePetCare
                                    }
                            )
                        }
                    )
                }
            }
        }
    }
}
