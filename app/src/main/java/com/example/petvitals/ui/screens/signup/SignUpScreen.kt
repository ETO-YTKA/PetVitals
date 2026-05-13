package com.example.petvitals.ui.screens.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
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
fun SignUpScreen(
    navigateToLogIn: () -> Unit,
    onPopBackStack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SignUpEvent.OnShowSnackbar -> snackbarHostState.showSnackbar(event.snackbarState)
        }
    }

    SignUpContent(
        uiState = uiState,
        onAction = { action -> viewModel.onAction(action, onSuccess = navigateToLogIn) },
        snackbarHostState = snackbarHostState,
        onPopBackStack = onPopBackStack,
        modifier = modifier
    )
}

@Composable
private fun SignUpContent(
    uiState: SignUpUiState,
    onAction: (SignUpAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    onPopBackStack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopBar(
                onPopBackStack = onPopBackStack
            )
        },
        snackbarHost = {
            CustomSnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier
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
            HeroSection()

            Spacer(modifier = Modifier.height(16.dp))

            SignUpForm(
                uiState = uiState,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun TopBar(
    onPopBackStack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            text = stringResource(R.string.join_petvitals),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(R.string.sign_up_hero),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun SignUpForm(
    uiState: SignUpUiState,
    onAction: (SignUpAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        CustomTextField(
            value = uiState.name,
            onValueChange = { onAction(SignUpAction.OnNameChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.name)) },
            leadingIcon = { Icon(painterResource(R.drawable.ic_person), contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            supportingText = uiState.nameErrorMessage?.let {
                { Text(text = it) }
            },
            isError = uiState.nameErrorMessage != null
        )

        CustomTextField(
            value = uiState.email,
            onValueChange = { onAction(SignUpAction.OnEmailChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.email)) },
            leadingIcon = { Icon(painterResource(R.drawable.ic_mail), contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            supportingText = uiState.emailErrorMessage?.let {
                { Text(text = it) }
            },
            isError = uiState.emailErrorMessage != null
        )

        CustomTextField(
            value = uiState.password,
            onValueChange = { onAction(SignUpAction.OnPasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.password)) },
            leadingIcon = { Icon(painterResource(R.drawable.ic_lock), contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            supportingText = uiState.passwordErrorMessage?.let {
                { Text(text = it) }
            },
            isError = uiState.passwordErrorMessage != null
        )

        CustomTextField(
            value = uiState.repeatPassword,
            onValueChange = { onAction(SignUpAction.OnRepeatPasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.repeat_password)) },
            leadingIcon = { Icon(painterResource(R.drawable.ic_lock), contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onAction(SignUpAction.SignUp) }
            ),
            supportingText = uiState.repeatPasswordErrorMessage?.let {
                { Text(text = it) }
            },
            isError = uiState.repeatPasswordErrorMessage != null
        )

        Spacer(modifier = Modifier.height(0.dp))

        CustomMediumButton(
            onClick = { onAction(SignUpAction.SignUp) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.sign_up),
                fontSize = Dimen.FontSize.mediumButton
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SignUpContentPreview() {
    PetVitalsTheme {
        SignUpContent(
            uiState = SignUpUiState(),
            onAction = {},
            snackbarHostState = SnackbarHostState(),
            onPopBackStack = {}
        )
    }
}
