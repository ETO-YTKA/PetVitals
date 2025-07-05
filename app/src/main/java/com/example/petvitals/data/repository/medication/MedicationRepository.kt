package com.example.petvitals.data.repository.medication

interface MedicationRepository {

    suspend fun getMedications(petId: String): List<Medication>
    suspend fun saveMedication(medication: Medication)
    suspend fun deleteMedication(medication: Medication)
    suspend fun getMedicationById(medicationId: String, petId: String): Medication?
}