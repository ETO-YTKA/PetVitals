package com.example.petvitals.domain.repository

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Medication

interface MedicationRepository {

    suspend fun getMedications(petId: String): AppResult<FirestoreError, List<Medication>>
    suspend fun saveMedication(medication: Medication)
    suspend fun deleteMedication(medication: Medication): AppResult<FirestoreError, Unit>
    suspend fun getMedicationById(medicationId: String, petId: String): Medication?
}
