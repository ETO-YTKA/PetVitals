package com.example.petvitals.ui.screens.log_in

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.theme.Dimen

@Composable
fun SignInScreen(
    navigateTo: (Any) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LogInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    ScreenLayout(
        columnModifier = modifier.verticalScroll(rememberScrollState()),
        topBar = { TopBar(title = stringResource(R.string.log_in)) }
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
        )

        Spacer(modifier = Modifier.height(Dimen.spaceMedium))
        CustomOutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text(text = stringResource(R.string.password)) },
            leadingIcon = { Icon(painter = painterResource(R.drawable.ic_password2), contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
        )
        uiState.errorMessage?.let { message ->
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
                viewModel.onLogInClick(navigateTo)

                uiState.errorMessage?.let { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text(text = stringResource(R.string.log_in))
        }

        TextButton(
            onClick = { viewModel.onSignUpClick(navigateTo) }
        ) {
            Text(text = stringResource(R.string.sign_up_new_account))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(title: String) {
    CenterAlignedTopAppBar(
        title = {
            Text(text = title)
        }
    )
}