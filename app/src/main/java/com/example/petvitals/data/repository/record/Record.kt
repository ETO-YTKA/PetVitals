package com.example.petvitals.data.repository.record

import androidx.annotation.StringRes
import com.example.petvitals.R
import java.util.Calendar

data class Record(
    val recordId: String? = null,
    val userId: String,
    val title: String = "",
    val type: RecordType = RecordType.NOTE,
    val date: Long = Calendar.getInstance().timeInMillis,
    val description: String
)

enum class RecordType(@StringRes val titleResId: Int) {
    VACCINATION(R.string.vaccination),
    MEDICATION(R.string.medication),
    VET_VISIT(R.string.vet_visit),
    SYMPTOM(R.string.symptom),
    GROOMING(R.string.grooming),
    INCIDENT(R.string.incident),
    NOTE(R.string.note)
}