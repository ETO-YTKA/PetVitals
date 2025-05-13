package com.example.petvitals.ui.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.petvitals.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarProfileSettings(
    title: String,
    onNavigateToSettings: () -> Unit,
    onNavigateToUserProfile: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(
                onClick = onNavigateToUserProfile
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = stringResource(R.string.user_profile)
                )
            }
        },
        actions = {
            IconButton(
                onClick = onNavigateToSettings
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.settings)
                )
            }
        }
    )
}