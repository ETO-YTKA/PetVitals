package com.example.petvitals.ui.screens.sharepet

import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.PetInvite

data class SharePetUiState(
    val isLoading: Boolean = false,

    val petId: String = "",
    val petMembers: List<Member> = emptyList(),
    val selectedInvitePermission: PermissionLevel = PermissionLevel.VIEWER,
    val latestGeneratedCode: String? = null,
    val activeInvites: List<PetInvite> = emptyList(),
    val removingMemberId: String? = null,

    val membersErrorMessageRes: Int? = null,
    val invitesErrorMessageRes: Int? = null,
    val createInviteErrorMessageRes: Int? = null,
    val permissionErrorMessageRes: Int? = null
)