package com.example.petvitals.domain.models

data class Member(
    val userId: String = "",
    val permissionLevel: PermissionLevel = PermissionLevel.VIEWER
)