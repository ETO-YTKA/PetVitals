package com.example.petvitals.data.repository.pet

import androidx.annotation.StringRes
import com.example.petvitals.R
import java.util.UUID

data class Pet(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String = "",
    val species: PetSpecies = PetSpecies.CAT,
    val birthDate: Map<String, Int> = emptyMap(),
    val imageString: String? = null
)

enum class PetSpecies(@StringRes val titleRes: Int) {
    CAT(R.string.cat),
    DOG(R.string.dog)
}