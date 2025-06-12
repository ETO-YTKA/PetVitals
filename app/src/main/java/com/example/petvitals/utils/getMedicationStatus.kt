package com.example.petvitals.utils

import com.example.petvitals.data.repository.medication.MedicationStatus
import java.util.Date

fun getMedicationStatus(startDate: Date?, endDate: Date?): MedicationStatus {
    val now = Date()

    return when {
        startDate == null && endDate == null -> MedicationStatus.REGULAR

        startDate == null && endDate != null -> {
            if (now.before(endDate)) MedicationStatus.ONGOING else MedicationStatus.COMPLETED
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