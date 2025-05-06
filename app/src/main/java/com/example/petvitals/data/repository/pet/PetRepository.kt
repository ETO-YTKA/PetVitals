package com.example.petvitals.data.repository.pet

import com.example.petvitals.data.Pet

interface PetRepository {
    suspend fun addPetToUser(userId: String, petName: String, species: String, birthDate: Map<String, Int>)
    suspend fun getUserPets(userId: String): List<Pet>
    suspend fun deletePet(userId: String, petId: String)
    suspend fun deleteAllPets(userId: String)
}