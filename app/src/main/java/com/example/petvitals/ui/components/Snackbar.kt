package com.example.petvitals.ui.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.petvitals.R
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.ui.theme.PetVitalsTheme

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

@Composable
fun CustomSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbarType: SnackbarType = SnackbarType.INFO
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { snackbarData ->
        val containerColor = when (snackbarType) {
            SnackbarType.ERROR -> MaterialTheme.colorScheme.errorContainer
            SnackbarType.SUCCESS -> MaterialTheme.colorScheme.tertiaryContainer
            SnackbarType.INFO -> MaterialTheme.colorScheme.secondaryContainer
        }

        val contentColor = when (snackbarType) {
            SnackbarType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
            SnackbarType.SUCCESS -> MaterialTheme.colorScheme.onTertiaryContainer
            SnackbarType.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
        }

        val iconRes = when (snackbarType) {
            SnackbarType.ERROR -> R.drawable.ic_error
            SnackbarType.SUCCESS -> R.drawable.ic_check
            SnackbarType.INFO -> R.drawable.ic_warning
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
                    text = snackbarData.visuals.message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                snackbarData.visuals.actionLabel?.let { actionLabel ->
                    TextButton(
                        onClick = { snackbarData.performAction() }
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
        message = snackbarState.message,
        actionLabel = snackbarState.actionLabel,
        withDismissAction = snackbarState.withDismissAction,
        duration = snackbarState.duration
    )

    when (result) {
        SnackbarResult.ActionPerformed -> snackbarState.onAction()
        SnackbarResult.Dismissed -> snackbarState.onDismiss()
    }
}

@PreviewLightDark
@Composable
private fun CustomSnackbarPreview() {
    val snackbarHostState = remember { SnackbarHostState() }

    PetVitalsTheme {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                message = "This is a test message",
                actionLabel = "Action",
                duration = SnackbarDuration.Indefinite
            )
        }

        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CustomSnackbarHost(
                    hostState = snackbarHostState,
                    snackbarType = SnackbarType.ERROR
                )
                CustomSnackbarHost(
                    hostState = snackbarHostState,
                    snackbarType = SnackbarType.INFO
                )
                CustomSnackbarHost(
                    hostState = snackbarHostState,
                    snackbarType = SnackbarType.SUCCESS
                )
            }
        }
    }
}
