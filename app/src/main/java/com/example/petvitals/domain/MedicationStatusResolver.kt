package com.example.petvitals.domain

import com.example.petvitals.domain.models.MedicationStatus
import java.util.Date

fun getMedicationStatus(startDate: Date?, endDate: Date?): MedicationStatus {
    val now = Date()

    return when {
        startDate == null && endDate == null -> MedicationStatus.REGULAR

        startDate != null && endDate == null -> {
            when {
                now.after(startDate) -> MedicationStatus.ONGOING
                else -> MedicationStatus.SCHEDULED
            }
        }

        else -> {
            when {
                now.after(endDate) -> MedicationStatus.COMPLETED
                now.before(startDate) -> MedicationStatus.SCHEDULED
                else -> MedicationStatus.ONGOING
            }
        }
    }
}