package com.example.petvitals.ui.screens.sharepet

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.petvitals.R
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.PetInvite

class SharePetPreviewParameterProvider : PreviewParameterProvider<SharePetUiState> {

    private val owner = Member("owner-id", "Morgan Lee", PermissionLevel.OWNER)
    private val editor = Member("editor-id", "Avery Johnson", PermissionLevel.EDITOR)
    private val viewer = Member(
        "viewer-id",
        "Riley with a very long family name",
        PermissionLevel.VIEWER
    )
    private val base = SharePetUiState(
        petId = "pet-id",
        petMembers = listOf(owner, editor, viewer)
    )

    override val values: Sequence<SharePetUiState> = sequenceOf(
        base,
        base.copy(petMembers = listOf(owner)),
        base.copy(
            selectedInvitePermission = PermissionLevel.EDITOR,
            latestGeneratedCode = "ABCD-EFGH-JKLM-NPQR"
        ),
        base.copy(
            activeInvites = listOf(
                PetInvite(
                    codeHash = "1",
                    petId = "invite-editor",
                    permissionLevel = PermissionLevel.EDITOR,
                ),
                PetInvite(
                    codeHash = "2",
                    petId = "invite-viewer",
                    permissionLevel = PermissionLevel.VIEWER,
                )
            )
        ),
        SharePetUiState(isLoading = true),
        SharePetUiState(
            permissionErrorMessageRes = R.string.pet_sharing_access_denied
        )
    )
}
