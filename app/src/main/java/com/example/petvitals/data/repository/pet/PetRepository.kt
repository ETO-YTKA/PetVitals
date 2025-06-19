package com.example.petvitals.data.repository.pet

interface PetRepository {
    suspend fun savePet(pet: Pet)
    suspend fun getPetById(petId: String): Pet?
    suspend fun deletePet(petId: String)
    suspend fun deleteAllUserPetsPets()
}