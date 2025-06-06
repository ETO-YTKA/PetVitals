package com.example.petvitals.data.repository.medication

interface MedicationRepository {

    suspend fun getMedications(petId: String): List<Medication>
    suspend fun addMedication(medication: Medication)
    suspend fun updateMedication(medication: Medication)
    suspend fun deleteMedication(medication: Medication)
}