package com.example.petvitals.ui.screens.log_in

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.ErrorMessage
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen

@Composable
fun SignInScreen(
    onNavigateToSplash: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToPasswordReset: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LogInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.showVerificationError) {
        if (uiState.showVerificationError) {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.email_not_verified_message),
                actionLabel = context.getString(R.string.resend), 
                duration = SnackbarDuration.Indefinite 
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    viewModel.onResendVerificationEmailClick()
                }
                SnackbarResult.Dismissed -> {}
            }
        }
    }

    ScreenLayout(
        columnModifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = Dimen.spaceMedium),
        topBar = { TopBar(title = { Text(stringResource(R.string.login)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState ) }
    ) {
        CustomOutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text(text = stringResource(R.string.email)) },
            leadingIcon = { Icon(painter = painterResource(R.drawable.ic_mail), contentDescription = null) },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Dimen.spaceMedium))
        CustomOutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text(text = stringResource(R.string.password)) },
            leadingIcon = { Icon(painter = painterResource(R.drawable.ic_password2), contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        uiState.errorMessage?.let { ErrorMessage(it) }

        Spacer(modifier = Modifier.height(Dimen.spaceLarge))

        Button(
            onClick = {
                viewModel.onLogInClick(onNavigateToSplash = onNavigateToSplash)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.log_in))
        }

        TextButton(
            onClick = { viewModel.onSignUpClick(onNavigateToSignUp = onNavigateToPasswordReset) }
        ) {
            Text(text = stringResource(R.string.forgot_password))
        }

        TextButton(
            onClick = { viewModel.onSignUpClick(onNavigateToSignUp = onNavigateToSignUp) }
        ) {
            Text(text = stringResource(R.string.dont_have_account_sign_up))
        }
    }
}