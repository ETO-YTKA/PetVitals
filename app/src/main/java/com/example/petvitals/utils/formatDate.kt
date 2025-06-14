package com.example.petvitals.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDateWithLocale(date: Date): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return sdf.format(date)
}

fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy")
    return sdf.format(date)
}