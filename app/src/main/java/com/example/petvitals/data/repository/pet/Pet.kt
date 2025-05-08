package com.example.petvitals.data.repository.pet

data class Pet(
    val id: String,
    val name: String,
    val species: String,
    val birthDate: Map<String, Int>
)