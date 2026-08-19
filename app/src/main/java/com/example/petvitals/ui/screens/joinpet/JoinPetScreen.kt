package com.example.petvitals.ui.screens.joinpet

import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.CenterAlignedTopBar
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomMediumButton
import com.example.petvitals.ui.components.CustomTextField
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.ui.theme.PetVitalsTheme
import com.example.petvitals.ui.utils.InviteCodeVisualTransformation

@Composable
fun JoinPetScreen(
    onPopBackStack: () -> Unit,
    onPetJoined: () -> Unit,
    viewModel: JoinPetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                JoinPetEvent.Joined -> onPetJoined()
            }
        }
    }

    JoinPetScreenContent(
        uiState = uiState,
        onAction = { viewModel.onAction(it) },
        onNavigateBack = onPopBackStack
    )
}

@Composable
internal fun JoinPetScreenContent(
    uiState: JoinPetUiState,
    onAction: (JoinPetAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(ClipboardManager::class.java)

    Scaffold(
        topBar = {
            CenterAlignedTopBar(
                title = { Text(stringResource(R.string.join_a_pet)) },
                navigationIcon = {
                    CustomIconButton(
                        onClick = onNavigateBack,
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = Dimen.Screen.horizontalPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lock),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.enter_invite_code),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.use_invite_code),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CustomTextField(
                        value = uiState.code,
                        onValueChange = { onAction(JoinPetAction.OnCodeChange(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSubmitting,
                        singleLine = true,
                        label = { Text(stringResource(R.string.invite_code)) },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.invite_code_placeholder),
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        trailingIcon = {
                            TextButton(
                                onClick = {
                                    val pastedText = clipboardManager.primaryClip
                                        ?.takeIf { it.itemCount > 0 }
                                        ?.getItemAt(0)
                                        ?.coerceToText(context)
                                        ?.toString()

                                    if (!pastedText.isNullOrBlank()) {
                                        onAction(JoinPetAction.OnCodeChange(pastedText))
                                    }
                                },
                                enabled = !uiState.isSubmitting,
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text(stringResource(R.string.paste))
                            }
                        },
                        supportingText = uiState.errorMessageRes?.let { messageRes ->
                            {
                                Text(
                                    text = stringResource(messageRes),
                                    modifier = Modifier.semantics {
                                        liveRegion = LiveRegionMode.Polite
                                    }
                                )
                            }
                        },
                        isError = uiState.errorMessageRes != null,
                        visualTransformation = InviteCodeVisualTransformation,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Done
                        )
                    )

                    CustomMediumButton(
                        onClick = { onAction(JoinPetAction.OnJoinPetClick) },
                        enabled = uiState.isCodeComplete && !uiState.isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = stringResource(R.string.joining_pet))
                        } else {
                            Text(text = stringResource(R.string.join_pet))
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun JoinPetScreenPreview(
    @PreviewParameter(JoinPetPreviewParameterProvider::class) uiState: JoinPetUiState
) {
    PetVitalsTheme {
        JoinPetScreenContent(
            uiState = uiState,
            onAction = {},
            onNavigateBack = {}
        )
    }
}
