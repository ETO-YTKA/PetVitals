package com.example.petvitals.ui.screens.sign_up

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.ScreenLayout

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
        Spacer(modifier = Modifier.height(16.dp))
        CustomOutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text(text = stringResource(R.string.email)) },
            leadingIcon = { Icon(painterResource(R.drawable.mail_24), contentDescription = null) },
            singleLine = true,
            isError = uiState.isEmailInvalid
        )
        Spacer(modifier = Modifier.height(4.dp))
        CustomOutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
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
        Spacer(modifier = Modifier.height(12.dp))
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