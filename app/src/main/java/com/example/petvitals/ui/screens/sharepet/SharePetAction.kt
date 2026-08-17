package com.example.petvitals.ui.screens.sharepet

import android.content.Context
import com.example.petvitals.domain.models.PermissionLevel

sealed interface SharePetAction {
    data class OnSelectInvitePermission(val permissionLevel: PermissionLevel) : SharePetAction
    data class OnRemoveMember(val userId: String) : SharePetAction
    data object OnCreateInviteCode : SharePetAction
    data class OnRevokeInviteCode(val codeId: String) : SharePetAction
    data class OnCopyInviteCode(val context: Context, val code: String) : SharePetAction
    data class OnShareInviteCode(val context: Context, val code: String) : SharePetAction
}
