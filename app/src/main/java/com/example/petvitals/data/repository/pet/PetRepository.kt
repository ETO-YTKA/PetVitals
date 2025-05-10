package com.example.petvitals.data.repository.pet

interface PetRepository {
    suspend fun addPetToUser(userId: String, petName: String, species: String, birthDate: Map<String, Int>)
    suspend fun getUserPets(userId: String): List<Pet>
    suspend fun getPetById(userId: String, petId: String): Pet?
    suspend fun updatePet(userId: String, pet: Pet)
    suspend fun deletePet(userId: String, petId: String)
    suspend fun deleteAllPets(userId: String)
}