package com.example.petvitals.ui.screens.pets

import com.example.petvitals.domain.models.Pet

data class PetsUiState(
    val pets: List<Pet> = emptyList(),

    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)
