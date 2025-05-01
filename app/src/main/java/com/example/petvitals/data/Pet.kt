package com.example.petvitals.data

data class Pet(
    val name: String,
    val species: String,
    val birthDate: Map<String, Int>
)