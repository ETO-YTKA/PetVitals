package com.example.petvitals.ui.screens.user_profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        viewModel.getUserData()
    }

    ScreenLayout(
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
            style = MaterialTheme.typography.titleMedium
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
    }
}