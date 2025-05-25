package com.example.petvitals.ui.screens.sign_up

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.ErrorMessage
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen

@Composable
fun SignUpScreen(
    onNavigateToLogIn: () -> Unit,
    onPopBackStack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val (trailingIcon, visualTransformation) = if (uiState.isPasswordHidden) {
        painterResource(R.drawable.ic_rounded_visibility) to PasswordVisualTransformation()
    } else {
        painterResource(R.drawable.ic_rounded_visibility_off) to VisualTransformation.None
    }

    ScreenLayout(
        columnModifier = modifier.verticalScroll(rememberScrollState()),
        topBar = {
            TopBar(
                title = { Text(text = stringResource(R.string.signup)) },
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
        CustomOutlinedTextField(
            value = uiState.displayName,
            onValueChange = { viewModel.onDisplayNameChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.name)) },
            leadingIcon = { Icon(painterResource(R.drawable.ic_person), contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            isError = uiState.isDisplayNameInvalid
        )
        uiState.displayNameErrorMessage?.let { ErrorMessage(it) }

        Spacer(modifier = Modifier.height(Dimen.spaceMedium))
        CustomOutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.email)) },
            leadingIcon = { Icon(painterResource(R.drawable.ic_mail), contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            isError = uiState.isEmailInvalid
        )
        uiState.emailErrorMessage?.let { ErrorMessage(it) }

        Spacer(modifier = Modifier.height(Dimen.spaceMedium))
        CustomOutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.password)) },
            leadingIcon = { Icon(painterResource(R.drawable.ic_password2), contentDescription = null) },
            trailingIcon = {
                IconButton(
                    onClick = { viewModel.onChangeVisibilityClick() }
                ) {
                    Icon(trailingIcon, contentDescription = null)
                }
            },
            visualTransformation = visualTransformation,
            singleLine = true,
            isError = uiState.isPasswordInvalid
        )
        uiState.passwordErrorMessage?.let { ErrorMessage(it) }

        Spacer(modifier = Modifier.height(Dimen.spaceLarge))
        Button(
            onClick = {
                viewModel.onSignUpClick(onNavigateToLogIn = onNavigateToLogIn)
            },
            enabled = uiState.signUpButtonEnabled
        ) {
            Text(text = stringResource(R.string.sign_up))
        }
        uiState.signUpErrorMessage?.let { ErrorMessage(it) }
    }
}