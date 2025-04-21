package com.example.petvitals.ui.screens.sign_up

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.theme.Dimen

@Composable
fun SignUpScreen(
    navigateTo: (Any) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val (trailingIcon, visualTransformation) = if (uiState.isPasswordHidden) {
        painterResource(R.drawable.rounded_visibility_24) to PasswordVisualTransformation()
    } else {
        painterResource(R.drawable.rounded_visibility_off_24) to VisualTransformation.None
    }

    ScreenLayout(modifier = modifier) {
        Text(
            text = stringResource(R.string.sign_up),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(Dimen.spaceLarge))
        CustomOutlinedTextField(
            value = uiState.displayName,
            onValueChange = { viewModel.onDisplayNameChange(it) },
            modifier = Modifier.width(280.dp),
            label = { Text(text = stringResource(R.string.name)) },
            leadingIcon = { Icon(painterResource(R.drawable.person_24dp), contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            isError = uiState.isDisplayNameInvalid
        )
        uiState.displayNameErrorMessage?.let { message ->
            Spacer(modifier = Modifier.height(Dimen.spaceSmall))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(Dimen.spaceMedium))
        CustomOutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            modifier = Modifier.width(280.dp),
            label = { Text(text = stringResource(R.string.email)) },
            leadingIcon = { Icon(painterResource(R.drawable.mail_24), contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            isError = uiState.isEmailInvalid
        )
        uiState.emailErrorMessage?.let { message ->
            Spacer(modifier = Modifier.height(Dimen.spaceSmall))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(Dimen.spaceSmall))
        CustomOutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            modifier = Modifier.width(280.dp),
            label = { Text(text = stringResource(R.string.password)) },
            leadingIcon = { Icon(painterResource(R.drawable.password_24), contentDescription = null) },
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
        uiState.passwordErrorMessage?.let { message ->
            Spacer(modifier = Modifier.height(Dimen.spaceSmall))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(Dimen.spaceLarge))
        Button(
            onClick = {
                viewModel.onSignUpClick(navigateTo)
            },
            enabled = uiState.signUpButtonEnabled
        ) {
            Text(text = stringResource(R.string.sign_up))
        }
    }
}