package com.example.petvitals.ui.screens.user_profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.theme.Dimen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        viewModel.getUserData()
    }

    if (uiState.showDeleteAccountModal) {
        BasicAlertDialog(
            onDismissRequest = { viewModel.showModal(false) },
        ) {
            Card {
                Column(Modifier.padding(Dimen.spaceLarge)) {
                    Text(text = stringResource(R.string.are_you_sure_you_want_to_delete_your_account))

                    Spacer(modifier = Modifier.height(Dimen.spaceHuge))
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.deleteAccount() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(text = stringResource(R.string.delete))
                        }

                        Button(onClick = { viewModel.showModal(false) }) {
                            Text(text = stringResource(R.string.cancel))
                        }
                    }
                }
            }
        }
    }

    ScreenLayout(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .size(Dimen.petIconSize)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        ) {
            Image(
                painter = painterResource(R.drawable.person_24dp),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(Dimen.spaceMedium))
        Text(
            text = uiState.displayName,
            style = MaterialTheme.typography.displaySmall
        )

        Spacer(modifier = Modifier.height(Dimen.spaceSmall))
        Text(
            text = uiState.email,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outlineVariant
        )


        Spacer(modifier = Modifier.height(Dimen.spaceExtraHuge))
        Button(
            onClick = { viewModel.logout() },
            modifier = Modifier.width(Dimen.buttonWidth),
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = stringResource(R.string.logout))
        }

        Spacer(modifier = Modifier.height(Dimen.spaceMedium))
        Button(
            onClick = { viewModel.showModal(true) },
            modifier = Modifier.width(Dimen.buttonWidth),
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = stringResource(R.string.delete_profile))
        }
    }
}