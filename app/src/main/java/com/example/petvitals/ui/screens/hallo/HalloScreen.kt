package com.example.petvitals.ui.screens.hallo

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.ScreenLayout
import androidx.compose.runtime.getValue

@Composable
fun HalloScreen(
    restartApp: (Any) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HalloViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    ScreenLayout(modifier = modifier) {

        Text(text = "Hallo😀 ${uiState.displayName}")
        IconButton(
            onClick = { viewModel.signOut() }
        ) {
            Icon(painterResource(R.drawable.logout_24dp), contentDescription = null)
        }
    }

    LaunchedEffect(Unit) { viewModel.initialize(restartApp) }
}