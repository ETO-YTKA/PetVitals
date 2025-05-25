package com.example.petvitals.data.repository.pet

import com.example.petvitals.data.service.account.AccountService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val accountService: AccountService
) : PetRepository {

    override suspend fun addPetToUser(pet: Pet) {

        firestore
            .collection("users").document(pet.userId)
            .collection("pets").document(pet.id)
            .set(pet)
            .await()
    }

    override suspend fun getUserPets(): List<Pet> {

        val userId = accountService.currentUserId
        return firestore
            .collection("users").document(userId)
            .collection("pets")
            .get()
            .await()
            .map { it.toObject<Pet>() }
    }

    override suspend fun getPetById(petId: String): Pet? {

        val userId = accountService.currentUserId

        return firestore
            .collection("users").document(userId)
            .collection("pets").document(petId)
            .get()
            .await()
            .toObject<Pet>()
    }

    override suspend fun updatePet(pet: Pet) {

        val userId = accountService.currentUserId
        firestore
            .collection("users").document(userId)
            .collection("pets").document(pet.id)
            .set(pet)
            .await()
    }

    override suspend fun deletePet(petId: String) {

        val userId = accountService.currentUserId
        firestore
            .collection("users").document(userId)
            .collection("pets").document(petId)
            .delete()
            .await()
    }

    override suspend fun deleteAllPets() {

        val userId = accountService.currentUserId
        firestore
            .collection("users").document(userId)
            .collection("pets")
            .get()
            .await().forEach { it.reference.delete() }
    }
}