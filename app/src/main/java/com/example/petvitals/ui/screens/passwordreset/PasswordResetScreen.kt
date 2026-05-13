package com.example.petvitals.ui.screens.passwordreset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomMediumButton
import com.example.petvitals.ui.components.CustomSnackbarHost
import com.example.petvitals.ui.components.CustomTextField
import com.example.petvitals.ui.components.showSnackbar
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.ui.theme.PetVitalsTheme
import com.example.petvitals.ui.utils.ObserveAsEvents

@Composable
fun PasswordResetScreen(
    onPopBackStack: () -> Unit,
    viewModel: PasswordResetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is PasswordResetEvent.OnShowSnackbar -> snackbarHostState.showSnackbar(event.snackbarState)
        }
    }

    ResetPasswordContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onPopBackStack = onPopBackStack,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun ResetPasswordContent(
    uiState: PasswordResetUiState,
    onAction: (PasswordResetAction) -> Unit,
    onPopBackStack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopBar(
                onPopBackStack = onPopBackStack
            )
        },
        snackbarHost = {
            CustomSnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimen.Screen.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            ResetPasswordHero()

            if (uiState.isPasswordResetEmailSent) {
                PasswordResetSuccess(
                    onBackToLoginClick = onPopBackStack,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                ResetPasswordForm(
                    uiState = uiState,
                    onEmailChange = { onAction(PasswordResetAction.OnEmailChange(it)) },
                    onSendPasswordResetEmail = { onAction(PasswordResetAction.OnSendPasswordResetEmail) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ResetPasswordHero(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_lock_reset),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(72.dp)
                    .padding(14.dp)
            )
        }

        Text(
            text = stringResource(R.string.reset_password),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.password_reset_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ResetPasswordForm(
    uiState: PasswordResetUiState,
    onEmailChange: (String) -> Unit,
    onSendPasswordResetEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CustomTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            enabled = !uiState.isLoading,
            label = { Text(text = stringResource(R.string.email)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_mail),
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            supportingText = uiState.errorMessage?.let {
                { Text(text = it) }
            },
            isError = uiState.errorMessage != null
        )

        CustomMediumButton(
            onClick = onSendPasswordResetEmail,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                LoadingIndicator(
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.send_password_reset_email),
                    fontSize = Dimen.FontSize.mediumButton
                )
            }
        }
    }
}

@Composable
private fun PasswordResetSuccess(
    onBackToLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_mark_email_read),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = stringResource(R.string.check_your_inbox),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.password_reset_success_body),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            TextButton(onClick = onBackToLoginClick) {
                Text(text = stringResource(R.string.back_to_login))
            }
        }
    }
}

@Composable
private fun TopBar(onPopBackStack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_pets),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.app_name),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        navigationIcon = {
            CustomIconButton(
                onClick = onPopBackStack,
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.back)
            )
        }
    )
}

@Preview
@Composable
private fun PasswordResetContentPreview() {
    PetVitalsTheme {
        ResetPasswordContent(
            uiState = PasswordResetUiState(),
            onAction = {},
            onPopBackStack = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview
@Composable
private fun PasswordResetSuccessPreview() {
    PetVitalsTheme {
        ResetPasswordContent(
            uiState = PasswordResetUiState(isPasswordResetEmailSent = true),
            onAction = {},
            onPopBackStack = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview
@Composable
private fun PasswordResetContentLoading() {
    PetVitalsTheme {
        ResetPasswordContent(
            uiState = PasswordResetUiState(isLoading = true),
            onAction = {},
            onPopBackStack = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
