package com.example.petvitals.ui.screens.user_profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.ButtonWithIcon
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen

@Composable
fun UserProfileScreen(
    onPopBackStack: () -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showDeleteAccountModal) {
        DeleteAccountModal(
            uiState = uiState,
            onPasswordChange = viewModel::onPasswordChange,
            onDismissRequest = { viewModel.showModal(false) },
            onConfirmDelete = viewModel::deleteAccount,
        )
    }

    ScreenLayout(
        columnModifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = Dimen.spaceMedium),
        verticalArrangement = Arrangement.Top,
        topBar = {
            TopBar(
                title = { Text(stringResource(R.string.profile)) },
                navigationIcon = {
                    CustomIconButton(
                        onClick = onPopBackStack,
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .size(Dimen.petIconSize)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        ) {
            Image(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(Dimen.spaceMedium))

        Text(
            text = uiState.username,
            style = MaterialTheme.typography.displaySmall
        )

        Spacer(modifier = Modifier.height(Dimen.spaceSmall))

        Text(
            text = uiState.email,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.alpha(0.7f)
        )

        Spacer(modifier = Modifier.height(Dimen.spaceLarge))

        ButtonWithIcon(
            onClick = { viewModel.sendPasswordResetEmail() },
            text = stringResource(R.string.reset_password),
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_lock_reset),
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Dimen.spaceMedium))

        ButtonWithIcon(
            onClick = { viewModel.logout() },
            text = stringResource(R.string.logout),
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_logout),
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.error),

        )

        Spacer(modifier = Modifier.height(Dimen.spaceMedium))

        ButtonWithIcon(
            onClick = { viewModel.showModal(true) },
            text = stringResource(R.string.delete_account),
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_delete_forever),
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.error)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountModal(
    uiState: UserProfileUiState,
    onPasswordChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = "Warning",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.delete_account_title),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium)
            ) {
                Text(
                    text = stringResource(R.string.delete_account_confirmation_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                CustomOutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(R.string.password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = uiState.passwordErrorMessage != null,
                    supportingText = uiState.passwordErrorMessage,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                enabled = uiState.password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.delete))
            }
        }
    )
}