package com.example.petvitals.domain.models

import com.google.firebase.firestore.Exclude

data class PetInvite(
    @Exclude
    val codeHash: String = "",
    val petId: String = "",
    val permissionLevel: PermissionLevel = PermissionLevel.VIEWER
)