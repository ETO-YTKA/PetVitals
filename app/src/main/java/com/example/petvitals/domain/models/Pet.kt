package com.example.petvitals.domain.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.petvitals.R
import com.google.firebase.firestore.Exclude
import java.util.UUID

data class Pet(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val species: PetSpecies = PetSpecies.CAT,
    val breed: String? = null,
    val gender: Gender? = null,
    val dobYear: Int? = null,
    val dobMonth: Int? = null,
    val dobDay: Int? = null,
    val avatar: String? = null,
    val healthNote: String? = null,
    val foodNote: String? = null,
    @get:Exclude
    val currentUserPermission: PermissionLevel = PermissionLevel.OWNER
)

enum class PetSpecies(@DrawableRes val drawableRes: Int, @StringRes val stringRes: Int) {
    CAT(R.drawable.ic_cat, R.string.cat),
    DOG(R.drawable.ic_dog, R.string.dog)
}

enum class Gender {
    MALE,
    FEMALE
}