package com.example.petvitals.domain.models

import java.util.UUID

data class Food(
    val id: String = UUID.randomUUID().toString(),
    val petId: String = "",
    val name: String = "",
    val portion: String = "",
    val frequency: String = "",
    val note: String = "",
)