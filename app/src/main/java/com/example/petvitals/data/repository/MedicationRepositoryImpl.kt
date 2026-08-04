package com.example.petvitals.data.repository

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
    private val firestore: FirebaseFirestore
) : MedicationRepository {

    override suspend fun getMedications(
        petId: String
    ): AppResult<FirestoreError, List<Medication>> = safeFirestoreCall {
        firestore
            .collection(FirestoreCollections.PETS)
            .document(petId)
            .collection(FirestoreCollections.MEDICATIONS)
            .get()
            .await()
            .map { it.toObject<Medication>() }
    }

    override suspend fun saveMedication(
        medication: Medication
    ): AppResult<FirestoreError, Unit> = safeFirestoreCall {
        firestore
            .collection(FirestoreCollections.PETS)
            .document(medication.petId)
            .collection(FirestoreCollections.MEDICATIONS)
            .document(medication.id)
            .set(medication)
            .await()
    }

    override suspend fun deleteMedication(
        medication: Medication
    ): AppResult<FirestoreError, Unit> = safeFirestoreCall {
        firestore
            .collection(FirestoreCollections.PETS)
            .document(medication.petId)
            .collection(FirestoreCollections.MEDICATIONS)
            .document(medication.id)
            .delete()
            .await()
    }

    override suspend fun getMedicationById(
        petId: String,
        medicationId: String
    ): AppResult<FirestoreError, Medication?> = safeFirestoreCall {
        firestore
            .collection(FirestoreCollections.PETS)
            .document(petId)
            .collection(FirestoreCollections.MEDICATIONS)
            .document(medicationId)
            .get()
            .await()
            .toObject<Medication>()
    }
}