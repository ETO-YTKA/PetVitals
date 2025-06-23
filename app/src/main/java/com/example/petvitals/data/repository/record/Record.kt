package com.example.petvitals.data.repository.record

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet_permission.PermissionLevel
import com.google.firebase.firestore.Exclude
import java.util.Date
import java.util.UUID

data class Record(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "",
    val type: RecordType = RecordType.NOTE,
    val date: Date = Date(),
    val description: String = "",
    val petIds: List<String> = emptyList(),
    @get:Exclude
    val currentUserPermission: PermissionLevel = PermissionLevel.OWNER
)

enum class RecordType(@StringRes val titleResId: Int, @DrawableRes val iconResId: Int, val color: Color) {
    VACCINATION(R.string.vaccination, R.drawable.ic_vaccines, Color(0xFF4CAF50)),
    MEDICATION(R.string.medication, R.drawable.ic_medication, Color(0xFF2196F3)),
    VET_VISIT(R.string.vet_visit, R.drawable.ic_medical_services,Color(0xFF00BCD4)),
    SYMPTOM(R.string.symptom, R.drawable.ic_sick,Color(0xFFFFC107)),
    GROOMING(R.string.grooming, R.drawable.ic_content_cut,Color(0xFFE91E63)),
    INCIDENT(R.string.incident, R.drawable.ic_warning,Color(0xFFF44336)),
    NOTE(R.string.note, R.drawable.ic_sticky_note,Color(0xFF9C27B0))
}