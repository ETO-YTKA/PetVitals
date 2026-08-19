package com.example.petvitals.data.utils

import com.example.petvitals.domain.models.Pet

fun Pet.toUpdateMap(): Map<String, Any?> =
    mapOf(
        "name" to name,
        "species" to species,
        "breed" to breed,
        "gender" to gender,
        "dobYear" to dobYear,
        "dobMonth" to dobMonth,
        "dobDay" to dobDay,
        "avatar" to avatar
    )