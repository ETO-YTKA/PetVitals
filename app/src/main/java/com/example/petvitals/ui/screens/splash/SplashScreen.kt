package com.example.petvitals.ui.screens.splash

import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.ui.components.ScreenLayout
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToMainApp: () -> Unit,
    onNavigateToLogIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel()
) {
    ScreenLayout(modifier = modifier) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }

    LaunchedEffect(true) {
        delay(500)
        viewModel.onAppStart(
            onNavigateToPets = onNavigateToMainApp,
            onNavigateToLogIn = onNavigateToLogIn
        )
    }
}