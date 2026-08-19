package com.example.petvitals.ui.screens.joinpet

sealed interface JoinPetEvent {
    data object Joined : JoinPetEvent
}
