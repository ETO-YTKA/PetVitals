package com.example.petvitals.data.repository.record

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.example.petvitals.R
import java.util.Date
import java.util.UUID

data class Record(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "",
    val type: RecordType = RecordType.NOTE,
    val date: Date = Date(),
    val description: String = "",
    val petsId: List<String> = emptyList()
)

enum class RecordType(@StringRes val titleResId: Int, val color: Color) {
    VACCINATION(R.string.vaccination, Color(0xFF4CAF50)),
    MEDICATION(R.string.medication, Color(0xFF2196F3)),
    VET_VISIT(R.string.vet_visit, Color(0xFF00BCD4)),
    SYMPTOM(R.string.symptom, Color(0xFFFFC107)),
    GROOMING(R.string.grooming, Color(0xFFE91E63)),
    INCIDENT(R.string.incident, Color(0xFFF44336)),
    NOTE(R.string.note, Color(0xFF9C27B0))
}