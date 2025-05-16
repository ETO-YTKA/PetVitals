package com.example.petvitals.data.repository.pet_image

interface PetImageRepository {
    suspend fun addPetImage(petImage: PetImage): String
    suspend fun getPetImageById(petImageId: String): PetImage?
}