package com.example.petvitals.ui.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.petvitals.ui.theme.PetVitalsTheme

@Composable
fun SplashScreen(
    onNavigateToMainApp: () -> Unit,
    onNavigateToLogIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel()
) {
    LaunchedEffect(true) {
        viewModel.onAppStart(
            onNavigateToPets = onNavigateToMainApp,
            onNavigateToLogIn = onNavigateToLogIn
        )
    }

    SplashScreenContent(modifier = modifier)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SplashScreenContent(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator(modifier = Modifier.size(100.dp))
        }
    }
}

@PreviewLightDark
@Composable
private fun SplashScreenContentPreview() {
    PetVitalsTheme {
        SplashScreenContent()
    }
}
