package com.example.petvitals.ui.screens.userprofile

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.domain.models.User
import com.example.petvitals.ui.components.CenterAlignedTopBar
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomMediumButton
import com.example.petvitals.ui.components.CustomSnackbarHost
import com.example.petvitals.ui.components.CustomTextField
import com.example.petvitals.ui.components.Loading
import com.example.petvitals.ui.components.ResetTopBarWhenNotScrollable
import com.example.petvitals.ui.components.rememberTopBarScrollBehavior
import com.example.petvitals.ui.components.showSnackbar
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.ui.theme.PetVitalsTheme
import com.example.petvitals.ui.utils.ObserveAsEvents

@Composable
fun UserProfileScreen(
    onPopBackStack: () -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is UserProfileEvent.OnShowSnackbar -> snackbarHostState.showSnackbar(event.snackbarState)
        }
    }

    UserProfileScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onPopBackStack = onPopBackStack
    )
}

@Composable
private fun UserProfileScreenContent(
    uiState: UserProfileUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (UserProfileAction) -> Unit,
    onPopBackStack: () -> Unit,
) {
    if (uiState.showDeleteAccountModal) {
        DeleteAccountModal(
            uiState = uiState,
            onPasswordChange = { onAction(UserProfileAction.OnPasswordChange(it)) },
            onDismissRequest = { onAction(UserProfileAction.ShowModal(false)) },
            onConfirmDelete = { onAction(UserProfileAction.DeleteAccount) },
        )
    }

    val scrollBehavior = rememberTopBarScrollBehavior()
    val contentScrollState = rememberScrollState()

    ResetTopBarWhenNotScrollable(
        scrollBehavior = scrollBehavior,
        canScrollBackward = contentScrollState.canScrollBackward,
        canScrollForward = contentScrollState.canScrollForward,
        contentVisible = !uiState.isLoading && uiState.user != null
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopBar(
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(R.string.profile)) },
                navigationIcon = {
                    CustomIconButton(
                        onClick = onPopBackStack,
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            )
        },
        snackbarHost = {
            CustomSnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        val user = uiState.user
        val errorMessageRes = uiState.errorMessageRes

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Loading()
            }
        } else if (user == null) {
            ErrorMessage(
                message = stringResource(errorMessageRes ?: R.string.unexpected_error),
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = Dimen.Screen.horizontalPadding)
            ) {
                Button(
                    onClick = { onAction(UserProfileAction.Retry) }
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .verticalScroll(contentScrollState)
                    .padding(paddingValues)
                    .padding(horizontal = Dimen.Screen.horizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(0.dp))

                UserInfo(user)

                ActionButtons(
                    modifier = Modifier.fillMaxWidth(),
                    onAction = onAction
                )
            }
        }
    }

}

@Composable
private fun UserInfo(
    user: User,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(150.dp),
            color = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        ) {
            Image(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = user.username,
            style = MaterialTheme.typography.displaySmall
        )

        Text(
            text = user.email,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.alpha(0.7f)
        )
    }
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    onAction: (UserProfileAction) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CustomMediumButton(
            onClick = { onAction(UserProfileAction.SendPasswordResetEmail) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.reset_password))
        }

        CustomMediumButton(
            onClick = { onAction(UserProfileAction.Logout) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.logout))
        }

        CustomMediumButton(
            onClick = { onAction(UserProfileAction.ShowModal(true)) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = stringResource(R.string.delete_account))
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(text = message)

        action()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteAccountModal(
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.delete_account_confirmation_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                CustomTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(R.string.password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = uiState.passwordErrorMessage != null,
                    supportingText = uiState.passwordErrorMessage?.let { { Text(it) } },
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

@PreviewLightDark
@Composable
private fun UserProfileScreenContentPreview() {
    PetVitalsTheme {
        UserProfileScreenContent(
            uiState = UserProfileUiState(
                user = User(
                    username = "username",
                    email = "email",
                    id = "id"
                ),
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onAction = {},
            onPopBackStack = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun UserProfileScreenContentErrorPreview() {
    PetVitalsTheme {
        UserProfileScreenContent(
            uiState = UserProfileUiState(
                isLoading = false,
                errorMessageRes = R.string.network_error
            ),
            snackbarHostState = SnackbarHostState(),
            onAction = {},
            onPopBackStack = {}
        )
    }
}
