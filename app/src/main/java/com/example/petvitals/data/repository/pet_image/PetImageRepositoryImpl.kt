package com.example.petvitals.data.repository.pet_image

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PetImageRepositoryImpl @Inject constructor() : PetImageRepository {

    override suspend fun addPetImage(petImage: PetImage): String {

        Firebase.firestore
            .collection("users").document(petImage.userId)
            .collection("images").document(petImage.id)
            .set(petImage)
            .await()

        return petImage.id
    }

    override suspend fun getPetImageById(petImageId: String): PetImage? {
        TODO("Not yet implemented")
    }
}