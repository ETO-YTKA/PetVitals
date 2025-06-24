package com.example.petvitals.ui.screens.password_reset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen

@Composable
fun PasswordResetScreen(
    onPopBackStack: () -> Unit,
    viewModel: PasswordResetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ScreenLayout(
        verticalArrangement = Arrangement.spacedBy(Dimen.spaceSmall),
        topBar = {
            TopBar(
                title = { Text(text = stringResource(R.string.password_reset)) },
                navigationIcon = {
                    IconButton(
                        onClick = onPopBackStack
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        columnModifier = Modifier
            .padding(vertical = Dimen.spaceMedium)
            .verticalScroll(rememberScrollState())
    ) {
        CustomOutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text(text = stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.errorMessage != null,
            supportingText = { uiState.errorMessage?.let { Text(it) } }
        )

        Button(
            onClick = { viewModel.onSendPasswordResetEmail() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.send_password_reset_email))
        }
    }
}