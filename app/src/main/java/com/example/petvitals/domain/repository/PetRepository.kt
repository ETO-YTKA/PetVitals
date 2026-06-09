package com.example.petvitals.domain.repository

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Pet

interface PetRepository {
    suspend fun savePet(pet: Pet): AppResult<FirestoreError, Unit>
    suspend fun getPetById(petId: String): AppResult<FirestoreError, Pet?>
    suspend fun getCurrentUserPets(): AppResult<FirestoreError, List<Pet>>
    suspend fun deletePet(petId: String): AppResult<FirestoreError, Unit>
}