package com.example.petvitals.domain.repository

import com.example.petvitals.domain.models.Pet

interface PetRepository {
    suspend fun savePet(pet: Pet)
    suspend fun getPetById(petId: String): Pet?
    suspend fun deletePet(petId: String)
    suspend fun deleteAllUserPetsPets()
}