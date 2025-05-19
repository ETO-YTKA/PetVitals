package com.example.petvitals.data.repository.pet

interface PetRepository {
    suspend fun addPetToUser(pet: Pet)
    suspend fun getUserPets(): List<Pet>
    suspend fun getPetById(petId: String): Pet?
    suspend fun updatePet(pet: Pet)
    suspend fun deletePet(petId: String)
    suspend fun deleteAllPets()
}