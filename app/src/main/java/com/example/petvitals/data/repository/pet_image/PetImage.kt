package com.example.petvitals.data.repository.pet_image

import java.util.UUID

data class PetImage(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val petId: String,
    val imageString: String
)