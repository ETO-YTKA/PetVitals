package com.example.petvitals.data.repository.food

import java.util.UUID

data class Feed(
    val id: String = UUID.randomUUID().toString(),
    val petId: String = "",
    val name: String = "",
    val portion: String = "",
    val frequency: String = "",
    val note: String = "",
)