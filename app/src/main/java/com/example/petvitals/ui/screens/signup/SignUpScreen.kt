package com.example.petvitals.ui.screens.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomMediumButton
import com.example.petvitals.ui.components.CustomSnackbarHost
import com.example.petvitals.ui.components.CustomTextField
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.ui.theme.PetVitalsTheme

@Composable
fun SignUpScreen(
    navigateToLogIn: () -> Unit,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    SignUpContent(
        uiState = uiState,
        updateDisplayName = { viewModel.updateDisplayName(it) },
        updateEmail = { viewModel.updateEmail(it) },
        updatePassword = { viewModel.updatePassword(it) },
        togglePasswordVisibility = { viewModel.togglePasswordVisibility() },
        signUp = { viewModel.signUp(onSuccess = navigateToLogIn) },
        snackbarHostState = snackbarHostState,
        popBackStack = popBackStack,
        modifier = modifier
    )

}

@Composable
private fun SignUpContent(
    uiState: SignUpUiState,
    updateDisplayName: (String) -> Unit,
    updateEmail: (String) -> Unit,
    updatePassword: (String) -> Unit,
    togglePasswordVisibility: () -> Unit,
    signUp: () -> Unit,
    snackbarHostState: SnackbarHostState,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (trailingIcon, visualTransformation) = if (uiState.isPasswordHidden) {
        painterResource(R.drawable.ic_rounded_visibility) to PasswordVisualTransformation()
    } else {
        painterResource(R.drawable.ic_rounded_visibility_off) to VisualTransformation.None
    }

    Scaffold(
        topBar = {
            TopBar(
                onPopBackStack = popBackStack
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
                .padding(horizontal = Dimen.Screen.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            CustomTextField(
                value = uiState.displayName,
                onValueChange = { updateDisplayName(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.name)) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_person), contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                supportingText = uiState.displayNameErrorMessage?.let {
                    { Text(text = it) }
                },
                isError = uiState.displayNameErrorMessage != null
            )

            CustomTextField(
                value = uiState.email,
                onValueChange = { updateEmail(it) },
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
                onValueChange = { updatePassword(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.password)) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_password2), contentDescription = null) },
                trailingIcon = {
                    IconButton(
                        onClick = togglePasswordVisibility
                    ) {
                        Icon(trailingIcon, contentDescription = null)
                    }
                },
                visualTransformation = visualTransformation,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                supportingText = uiState.passwordErrorMessage?.let {
                    { Text(text = it) }
                },
                isError = uiState.passwordErrorMessage != null
            )

            CustomMediumButton(
                onClick = {
                    signUp()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.sign_up),
                    fontSize = Dimen.FontSize.mediumButton
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    onPopBackStack: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(R.string.signup)) },
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

@PreviewLightDark
@Composable
private fun SignUpContentPreview() {
    PetVitalsTheme {
        SignUpContent(
            uiState = SignUpUiState(),
            updateDisplayName = {},
            updateEmail = {},
            updatePassword = {},
            togglePasswordVisibility = {},
            signUp = {},
            snackbarHostState = SnackbarHostState(),
            popBackStack = {}
        )
    }
}
