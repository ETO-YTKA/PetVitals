package com.example.petvitals.data.repository.pet

import java.util.UUID

data class Pet(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val species: String,
    val birthDate: Map<String, Int>,
    val imageString: String? = null
)