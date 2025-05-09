package com.example.petvitals

import kotlinx.serialization.Serializable

enum class Routes {
    LogIn,
    SignUp,
    Splash,
    Pets,
    UserProfile,
    AddPet,
    PetProfile
}

@Serializable
object LogIn

@Serializable
object SignUp

@Serializable
object Splash

@Serializable
object Pets

@Serializable
object UserProfile

@Serializable
object AddPet

@Serializable
data class PetProfile(val petId: String)