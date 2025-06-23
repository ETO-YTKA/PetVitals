package com.example.petvitals.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDateToStringLocale(date: Date, pattern: String = "dd MMMM yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(date)
}

fun formatDateToString(date: Date, pattern: String = "dd MMMM yyyy"): String {
    val sdf = SimpleDateFormat(pattern)
    return sdf.format(date)
}