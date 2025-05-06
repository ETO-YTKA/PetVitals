package com.example.petvitals.data.repository.pet

import com.example.petvitals.data.Pet
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor() : PetRepository {

    override suspend fun addPetToUser(
        userId: String,
        name: String,
        species: String,
        birthDate: Map<String, Int>
    ) {
        Firebase.firestore.collection("users").document(userId).collection("pets").add(
            hashMapOf(
                "name" to name,
                "species" to species,
                "birthDate" to birthDate
            )
        ).await()
    }

    override suspend fun getUserPets(userId: String): List<Pet> {
        return Firebase.firestore.collection("users").document(userId).collection("pets").get().await()
            .map {
                Pet(
                    name = it.data["name"] as String,
                    species = it.data["species"] as String,
                    birthDate = it.data["birthDate"] as Map<String, Int>
                )
            }
    }

    override suspend fun deletePet(userId: String, petId: String) {
        Firebase.firestore.collection("users").document(userId).collection("pets").document(petId).delete().await()
    }

    override suspend fun deleteAllPets(userId: String) {
        Firebase.firestore.collection("users").document(userId).collection("pets").get().await().forEach {
            it.reference.delete()
        }
    }
}