package com.example.petvitals.data.repository.pet

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PetRepository {

    override suspend fun savePet(pet: Pet) {

        firestore
            .collection("pets").document(pet.id)
            .set(pet)
            .await()
    }

    override suspend fun getPetById(petId: String): Pet? {

        return firestore
            .collection("pets").document(petId)
            .get()
            .await()
            .toObject<Pet>()
    }

    override suspend fun deletePet(petId: String) {

        firestore
            .collection("pets").document(petId)
            .delete()
            .await()
    }

    override suspend fun deleteAllUserPetsPets() {
        TODO("Not yet implemented")
    }
}