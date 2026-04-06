package com.example.petvitals.domain.repository

import com.example.petvitals.domain.models.Medication

interface MedicationRepository {

    suspend fun getMedications(petId: String): List<Medication>
    suspend fun saveMedication(medication: Medication)
    suspend fun deleteMedication(medication: Medication)
    suspend fun getMedicationById(medicationId: String, petId: String): Medication?
}