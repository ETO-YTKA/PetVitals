package com.example.petvitals.data.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Medication
import com.example.petvitals.domain.models.canManagePetCare
import com.example.petvitals.domain.repository.MedicationRepository
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import com.example.petvitals.domain.usecase.SaveMedicationUseCase
import jakarta.inject.Inject

class SaveMedicationUseCaseImpl @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val getPetPermissionUseCase: GetPetPermissionUseCase
) : SaveMedicationUseCase {

    override suspend fun invoke(
        medication: Medication
    ): AppResult<FirestoreError, Unit> =
        when (val permission = getPetPermissionUseCase(medication.petId)) {
            is AppResult.Failure -> permission
            is AppResult.Success -> {
                if (permission.data.canManagePetCare) {
                    medicationRepository.saveMedication(medication)
                } else {
                    AppResult.Failure(FirestoreError.PermissionDenied)
                }
            }
        }
}
