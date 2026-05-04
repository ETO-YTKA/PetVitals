package com.example.petvitals.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.petvitals.R
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.ui.theme.PetVitalsTheme
import com.example.petvitals.ui.theme.onSuccessContainerDark
import com.example.petvitals.ui.theme.onSuccessContainerLight
import com.example.petvitals.ui.theme.successContainerDark
import com.example.petvitals.ui.theme.successContainerLight

data class SnackbarState(
    val message: String,
    val actionLabel: String? = null,
    val snackbarType: SnackbarType = SnackbarType.INFO,
    val withDismissAction: Boolean = true,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val onAction: () -> Unit = {},
    val onDismiss: () -> Unit = {}
)

enum class SnackbarType {
    ERROR,
    SUCCESS,
    INFO
}

private data class CustomSnackbarVisuals(
    override val message: String,
    override val actionLabel: String?,
    val snackbarType: SnackbarType,
    override val withDismissAction: Boolean,
    override val duration: SnackbarDuration
) : SnackbarVisuals

@Composable
fun CustomSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { snackbarData ->
        val visuals = snackbarData.visuals
        val snackbarType = (visuals as? CustomSnackbarVisuals)?.snackbarType ?: SnackbarType.INFO

        val containerColor = when (snackbarType) {
            SnackbarType.ERROR -> MaterialTheme.colorScheme.errorContainer
            SnackbarType.SUCCESS -> if (isSystemInDarkTheme()) successContainerDark else successContainerLight
            SnackbarType.INFO -> MaterialTheme.colorScheme.secondaryContainer
        }

        val contentColor = when (snackbarType) {
            SnackbarType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
            SnackbarType.SUCCESS -> if (isSystemInDarkTheme()) onSuccessContainerDark else onSuccessContainerLight
            SnackbarType.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
        }

        val iconRes = when (snackbarType) {
            SnackbarType.ERROR -> R.drawable.ic_report
            SnackbarType.SUCCESS -> R.drawable.ic_check_circle
            SnackbarType.INFO -> R.drawable.ic_info
        }

        Snackbar(
            containerColor = containerColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(
                horizontal = Dimen.Screen.horizontalPadding,
                vertical = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = contentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = visuals.message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (visuals.withDismissAction) {
                        IconButton(
                            onClick = snackbarData::dismiss
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = stringResource(R.string.dismiss),
                                modifier = Modifier.size(18.dp),
                                tint = contentColor
                            )
                        }
                    }
                }

                visuals.actionLabel?.let { actionLabel ->
                    TextButton(
                        onClick = { snackbarData.performAction() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = actionLabel,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

suspend fun SnackbarHostState.showSnackbar(
    snackbarState: SnackbarState
) {
    val result = this.showSnackbar(
        CustomSnackbarVisuals(
            message = snackbarState.message,
            actionLabel = snackbarState.actionLabel,
            snackbarType = snackbarState.snackbarType,
            withDismissAction = snackbarState.withDismissAction,
            duration = snackbarState.duration
        )
    )

    when (result) {
        SnackbarResult.ActionPerformed -> snackbarState.onAction()
        SnackbarResult.Dismissed -> snackbarState.onDismiss()
    }
}

@PreviewLightDark
@Composable
private fun CustomSnackbarPreview() {
    PetVitalsTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SnackbarPreviewItem(
                    snackbarState = SnackbarState(
                        message = "Reminder added. You can adjust it anytime.",
                        snackbarType = SnackbarType.INFO,
                        duration = SnackbarDuration.Indefinite
                    )
                )
                SnackbarPreviewItem(
                    snackbarState = SnackbarState(
                        message = "Bella's weight entry was saved.",
                        snackbarType = SnackbarType.SUCCESS,
                        withDismissAction = false,
                        duration = SnackbarDuration.Indefinite
                    )
                )
                SnackbarPreviewItem(
                    snackbarState = SnackbarState(
                        message = "We couldn't verify your email yet.",
                        actionLabel = "Resend",
                        snackbarType = SnackbarType.ERROR,
                        duration = SnackbarDuration.Indefinite
                    )
                )
                SnackbarPreviewItem(
                    snackbarState = SnackbarState(
                        message = "Milo's medication reminder was saved, but notification permission is off.",
                        actionLabel = "Review settings",
                        snackbarType = SnackbarType.INFO,
                        duration = SnackbarDuration.Indefinite
                    )
                )
            }
        }
    }
}

@Composable
private fun SnackbarPreviewItem(
    snackbarState: SnackbarState,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(
        snackbarState.message,
        snackbarState.actionLabel,
        snackbarState.snackbarType,
        snackbarState.withDismissAction,
        snackbarState.duration
    ) {
        snackbarHostState.showSnackbar(snackbarState)
    }

    CustomSnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier.fillMaxWidth()
    )
}
