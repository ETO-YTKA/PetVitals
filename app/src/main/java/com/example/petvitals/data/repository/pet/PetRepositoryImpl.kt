package com.example.petvitals.data.repository.pet

import com.example.petvitals.data.service.account.AccountService
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val accountService: AccountService
) : PetRepository {

    override suspend fun addPetToUser(pet: Pet) {

        Firebase.firestore
            .collection("users").document(pet.userId)
            .collection("pets").document(pet.id)
            .set(pet)
            .await()
    }

    override suspend fun getUserPets(): List<Pet> {

        val userId = accountService.currentUserId
        return Firebase.firestore
            .collection("users").document(userId)
            .collection("pets")
            .get()
            .await()
            .map {
                Pet(
                    id = it.id,
                    userId = it.data["userId"] as String,
                    name = it.data["name"] as String,
                    species = it.data["species"] as String,
                    birthDate = it.data["birthDate"] as Map<String, Int>,
                    imageString = it.data["imageString"] as String?
                )
            }
    }

    override suspend fun getPetById(petId: String): Pet? {

        val userId = accountService.currentUserId

        val doc = Firebase.firestore
            .collection("users").document(userId)
            .collection("pets").document(petId)
            .get()
            .await()

        return if (doc.exists()) {
            Pet(
                id = doc.id,
                userId = (doc.data?.get("userId") ?: "") as String,
                name = (doc.data?.get("name") ?: "") as String,
                species = (doc.data?.get("species") ?: "") as String,
                birthDate = (doc.data?.get("birthDate") ?: "") as Map<String, Int>,
                imageString = (doc.data?.get("imageString") ?: "") as String?
            )
        } else {
            null
        }
    }

    override suspend fun updatePet(pet: Pet) {

        val userId = accountService.currentUserId
        Firebase.firestore
            .collection("users").document(userId)
            .collection("pets").document(pet.id)
            .set(pet)
            .await()
    }

    override suspend fun deletePet(petId: String) {

        val userId = accountService.currentUserId
        Firebase.firestore
            .collection("users").document(userId)
            .collection("pets").document(petId)
            .delete()
            .await()
    }

    override suspend fun deleteAllPets() {

        val userId = accountService.currentUserId
        Firebase.firestore
            .collection("users").document(userId)
            .collection("pets")
            .get()
            .await().forEach { it.reference.delete() }
    }
}