package com.example.petvitals

import kotlinx.serialization.Serializable

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
data class AddEditPet(val petId: String? = null)

@Serializable
data class PetProfile(val petId: String)

@Serializable
object CreateRecord

@Serializable
object Records

@Serializable
object MainApp