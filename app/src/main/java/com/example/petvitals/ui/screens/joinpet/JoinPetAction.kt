package com.example.petvitals.ui.screens.joinpet

sealed interface JoinPetAction {
    data class OnCodeChange(val code: String) : JoinPetAction
    object OnJoinPetClick : JoinPetAction
}