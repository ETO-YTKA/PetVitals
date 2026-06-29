package com.example.petvitals.ui.screens.pets

sealed interface PetsAction {
    data object RefreshPets : PetsAction
}
