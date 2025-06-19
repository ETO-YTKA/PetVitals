package com.example.petvitals.data.repository.pet_permissions

import com.example.petvitals.R
import java.util.UUID

data class PetPermissions(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val petId: String = "",
    val permissionLevel: PermissionLevel = PermissionLevel.OWNER
)

enum class PermissionLevel(val nameResId: Int) {
    OWNER(R.string.permission_level_owner),
    EDITOR(R.string.permission_level_editor),
    VIEWER(R.string.permission_level_viewer)
}