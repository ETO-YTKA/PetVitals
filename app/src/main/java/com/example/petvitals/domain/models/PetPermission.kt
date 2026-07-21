package com.example.petvitals.domain.models

import com.example.petvitals.R
import java.util.UUID

data class PetPermission(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val petId: String = "",
    val permissionLevel: PermissionLevel = PermissionLevel.VIEWER
)

enum class PermissionLevel(val nameResId: Int) {
    OWNER(R.string.permission_level_owner),
    EDITOR(R.string.permission_level_editor),
    VIEWER(R.string.permission_level_viewer)
}

val PermissionLevel.canManagePetCare: Boolean
    get() = this != PermissionLevel.VIEWER

val PermissionLevel.canDeletePet: Boolean
    get() = this == PermissionLevel.OWNER
