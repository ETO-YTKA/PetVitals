package com.example.petvitals.data.repository

import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.data.utils.safeFirestoreCall
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Medication
import com.example.petvitals.domain.repository.MedicationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class MedicationRepositoryImpl @Inject constructor(
    private val accountService: AccountService,
    private val firestore: FirebaseFirestore
) : MedicationRepository {

    override suspend fun getMedications(
        petId: String
    ): AppResult<FirestoreError, List<Medication>> = safeFirestoreCall {
        firestore
            .collection("pets").document(petId)
            .collection("medications")
            .get()
            .await()
            .map { it.toObject<Medication>() }
    }

    override suspend fun saveMedication(medication: Medication) {

        accountService.currentUserId
        firestore
            .collection("pets").document(medication.petId)
            .collection("medications").document(medication.id)
            .set(medication)
            .await()
    }

    override suspend fun deleteMedication(
        medication: Medication
    ): AppResult<FirestoreError, Unit> = safeFirestoreCall<Unit> {
        firestore
            .collection("pets").document(medication.petId)
            .collection("medications").document(medication.id)
            .delete()
            .await()
    }

    override suspend fun getMedicationById(
        medicationId: String,
        petId: String
    ): Medication? {

        return firestore
            .collection("pets").document(petId)
            .collection("medications").document(medicationId)
            .get()
            .await()
            .toObject<Medication>()
    }
}
