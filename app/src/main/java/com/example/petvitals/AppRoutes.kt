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
data class AddEditRecord(val recordId: String? = null)

@Serializable
object Records

@Serializable
object MainApp

@Serializable
data class SharePet(val petId: String)

@Serializable
object PasswordReset