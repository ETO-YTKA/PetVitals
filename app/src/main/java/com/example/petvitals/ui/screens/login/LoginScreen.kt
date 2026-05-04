package com.example.petvitals.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.CustomMediumButton
import com.example.petvitals.ui.components.CustomSnackbarHost
import com.example.petvitals.ui.components.CustomTextField
import com.example.petvitals.ui.components.SnackbarType
import com.example.petvitals.ui.components.showSnackbar
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.ui.theme.PetVitalsTheme
import com.example.petvitals.ui.utils.ObserveAsEvents

@Composable
fun LoginScreen(
    navigateToSplash: () -> Unit,
    navigateToSignUp: () -> Unit,
    navigateToPasswordReset: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarType by remember { mutableStateOf(SnackbarType.INFO) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is LoginEvent.OnError -> {
                snackbarType = event.snackbarState.snackbarType
                snackbarHostState.showSnackbar(event.snackbarState)
            }
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onAction = { action -> viewModel.onAction(action, onSuccess = navigateToSplash) },
        onSignUpClick = navigateToSignUp,
        onForgotPasswordClick = navigateToPasswordReset,
        snackbarHostState = snackbarHostState,
        snackbarType = snackbarType,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoginScreenContent(
    uiState: LoginUiState,
    onAction: (LoginAction) -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    snackbarType: SnackbarType,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopBar() },
        snackbarHost = {
            CustomSnackbarHost(
                hostState = snackbarHostState,
                snackbarType = snackbarType
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimen.Screen.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeroSection()

                Spacer(modifier = Modifier.height(16.dp))

                LoginForm(
                    uiState = uiState,
                    onAction = onAction,
                    onForgotPasswordClick = onForgotPasswordClick
                )
            }

            TextButton(
                onClick = onSignUpClick,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(text = stringResource(R.string.dont_have_account_sign_up))
            }
        }
    }
}

@Composable
private fun TopBar(modifier: Modifier = Modifier) {
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
        modifier = modifier
    )
}

@Composable
private fun HeroSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_pets),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(75.dp)
                    .padding(12.dp)
            )
        }

        Text(
            text = stringResource(R.string.welcome_back),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(R.string.log_in_to_continue),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun LoginForm(
    uiState: LoginUiState,
    onAction: (LoginAction) -> Unit,
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        CustomTextField(
            value = uiState.email,
            onValueChange = { onAction(LoginAction.OnEmailChanged(it)) },
            label = { Text(text = stringResource(R.string.email)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_mail),
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Password forgot password
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            CustomTextField(
                value = uiState.password,
                onValueChange = { onAction(LoginAction.OnPasswordChanged(it)) },
                label = { Text(text = stringResource(R.string.password)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_password2),
                        contentDescription = null
                    )
                },
                keyboardActions = KeyboardActions(
                    onDone = { onAction(LoginAction.Authenticate) }
                ),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(
                onClick = onForgotPasswordClick
            ) {
                Text(text = stringResource(R.string.forgot_password))
            }
        }

        CustomMediumButton(
            onClick = { onAction(LoginAction.Authenticate) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.log_in),
                fontSize = Dimen.FontSize.mediumButton
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    PetVitalsTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onAction = {},
            onSignUpClick = {},
            onForgotPasswordClick = {},
            snackbarHostState = SnackbarHostState(),
            snackbarType = SnackbarType.INFO,
            modifier = Modifier
        )
    }
}
