package com.example.petvitals.data.repository.medication

import java.util.Date
import java.util.UUID

data class Medication(
    val id: String = UUID.randomUUID().toString(),
    val petId: String = "",
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val startDate: Date? = null,
    val endDate: Date? = null,
    val note: String = ""
)

enum class MedicationStatus {
    ONGOING,
    SCHEDULED,
    COMPLETED,
    REGULAR
}