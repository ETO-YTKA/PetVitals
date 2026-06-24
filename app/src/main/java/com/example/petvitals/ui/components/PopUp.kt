package com.example.petvitals.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.example.petvitals.R
import com.example.petvitals.ui.theme.LocalCustomColorsScheme
import com.example.petvitals.ui.theme.PetVitalsTheme

enum class PopUpType {
    INFO,
    WARNING,
    ALERT,
    SUCCESS
}

data class PopUpState<Action>(
    val type: PopUpType,
    val title: String,
    val message: String,
    val primaryButton: PopUpButton<Action>? = null,
    val secondaryButton: PopUpButton<Action>? = null
)

data class PopUpButton<Action>(
    val text: String,
    val action: Action,
    val dismissAfterClick: Boolean = true
)

@Composable
fun <Action> PopUpHost(
    popUpState: PopUpState<Action>?,
    onAction: (Action) -> Unit,
    onDismiss: () -> Unit
) {
    val state = popUpState ?: return

    fun handleButtonClick(button: PopUpButton<Action>) {
        onAction(button.action)
        if (button.dismissAfterClick) {
            onDismiss()
        }
    }

    PopUp(
        type = state.type,
        title = state.title,
        message = state.message,
        onDismiss = onDismiss,
        confirmButtonText = state.primaryButton?.text,
        onConfirm = { state.primaryButton?.let(::handleButtonClick) },
        cancelButtonText = state.secondaryButton?.text,
        onCancel = { state.secondaryButton?.let(::handleButtonClick) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopUp(
    type: PopUpType,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmButtonText: String? = null,
    onConfirm: () -> Unit = {},
    cancelButtonText: String? = null,
    onCancel: () -> Unit = {}
) {
    val iconId = remember(type) {
        when (type) {
            PopUpType.INFO -> R.drawable.ic_info
            PopUpType.WARNING -> R.drawable.ic_info
            PopUpType.ALERT -> R.drawable.ic_warning
            PopUpType.SUCCESS -> R.drawable.ic_check
        }
    }

    val containerColor = when (type) {
        PopUpType.INFO -> LocalCustomColorsScheme.current.infoContainer
        PopUpType.WARNING -> LocalCustomColorsScheme.current.warningContainer
        PopUpType.ALERT -> MaterialTheme.colorScheme.errorContainer
        PopUpType.SUCCESS -> LocalCustomColorsScheme.current.successContainer
    }

    val iconColor = when (type) {
        PopUpType.INFO -> LocalCustomColorsScheme.current.onInfoContainer
        PopUpType.WARNING -> LocalCustomColorsScheme.current.onWarningContainer
        PopUpType.ALERT -> MaterialTheme.colorScheme.onErrorContainer
        PopUpType.SUCCESS -> LocalCustomColorsScheme.current.onSuccessContainer
    }

    BasicAlertDialog(
        onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = containerColor
                ) {
                    Icon(
                        painter = painterResource(iconId),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(12.dp)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    confirmButtonText?.let {
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = confirmButtonText)
                        }
                    }

                    cancelButtonText?.let {
                        Button(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors().copy(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(text = cancelButtonText)
                        }
                    }
                }
            }
        }
    }
}

private data class PopUpPreviewData(
    val type: PopUpType,
    val title: String,
    val message: String,
    val dismissText: String,
    val confirmText: String? = null
)

private class PopUpPreviewParameterProvider : PreviewParameterProvider<PopUpPreviewData> {
    override val values: Sequence<PopUpPreviewData> = sequenceOf(
        PopUpPreviewData(
            type = PopUpType.INFO,
            title = "Medication reminder",
            message = "Milo is due for his flea prevention treatment today.",
            dismissText = "Got it"
        ),
        PopUpPreviewData(
            type = PopUpType.WARNING,
            title = "Unsaved changes",
            message = "You edited Luna's feeding schedule. Save your changes before leaving this screen?",
            confirmText = "Save",
            dismissText = "Discard"
        ),
        PopUpPreviewData(
            type = PopUpType.ALERT,
            title = "Delete health record?",
            message = "This vaccination record will be permanently removed from Bella's care history.",
            confirmText = "Delete",
            dismissText = "Cancel"
        ),
        PopUpPreviewData(
            type = PopUpType.SUCCESS,
            title = "Pet profile added",
            message = "Charlie's profile is ready. You can now add food, medication, and care records.",
            dismissText = "Done"
        )
    )
}

@PreviewLightDark
@Composable
private fun PopUpPreview(
    @PreviewParameter(PopUpPreviewParameterProvider::class) data: PopUpPreviewData
) {
    PetVitalsTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            PopUp(
                type = data.type,
                title = data.title,
                message = data.message,
                onConfirm = {},
                onDismiss = {},
                confirmButtonText = data.confirmText,
                cancelButtonText = data.dismissText
            )
        }
    }
}
