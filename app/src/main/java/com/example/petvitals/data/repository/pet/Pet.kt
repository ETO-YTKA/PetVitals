package com.example.petvitals.data.repository.pet

import androidx.annotation.StringRes
import com.example.petvitals.R
import java.util.Calendar
import java.util.UUID

data class Pet(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String = "",
    val species: PetSpecies = PetSpecies.CAT,
    val breed: String = "",
    val gender: Gender = Gender.MALE,
    val dobMillis: Long = Calendar.getInstance().timeInMillis,
    val dobPrecision: DobPrecision = DobPrecision.EXACT,
    val imageString: String? = null
)

enum class PetSpecies(@StringRes val titleRes: Int) {
    CAT(R.string.cat),
    DOG(R.string.dog)
}

enum class DobPrecision(val isApproximate: Boolean) {
    EXACT(false),
    YEAR_MONTH(true),
    YEAR(true)
}

enum class Gender {
    MALE,
    FEMALE
}