package com.example.petvitals.domain.models

data class RecordOverview(
    val record: Record,
    val pets: List<Pet>,
    val canManage: Boolean
)