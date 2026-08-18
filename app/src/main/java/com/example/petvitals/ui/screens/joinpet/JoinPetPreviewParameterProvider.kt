package com.example.petvitals.ui.screens.joinpet

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.petvitals.R

internal class JoinPetPreviewParameterProvider : PreviewParameterProvider<JoinPetUiState> {
    override val values: Sequence<JoinPetUiState> = sequenceOf(
        JoinPetUiState(),
        JoinPetUiState(code = "ABCD"),
        JoinPetUiState(code = "ABCD1234EFGH5678"),
        JoinPetUiState(
            code = "ABCD1234EFGH5678",
            errorMessageRes = R.string.invalid_invite_code
        ),
        JoinPetUiState(code = "ABCD1234EFGH5678", isSubmitting = true)
    )
}
