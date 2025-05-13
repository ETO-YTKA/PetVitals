package com.example.petvitals.ui.screens.records

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.petvitals.R
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBarProfileSettings

@Composable
fun RecordsScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    ScreenLayout(
        topBar = {
            TopBarProfileSettings(
                title = stringResource(R.string.records),
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToUserProfile = onNavigateToProfile
            )
        }
    ) {

    }
}