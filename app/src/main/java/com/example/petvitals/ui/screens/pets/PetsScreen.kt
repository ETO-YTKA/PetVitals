package com.example.petvitals.ui.screens.pets

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.theme.Dimen

@Composable
fun PetsScreen(
    restartApp: (Any) -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetProfile: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToUserProfile: () -> Unit,
    viewModel: PetsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.initialize(restartApp) }

    ScreenLayout(topBar = { TopBar(
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToUserProfile = onNavigateToUserProfile
    ) }) {
        val pets = uiState.pets

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = Dimen.petIconSize),
            modifier = Modifier.fillMaxSize()
        ) {
            items(pets.size) { index ->
                val pet = pets[index]
                PetProfile(
                    petImage = painterResource(id = R.drawable.splash),
                    name = pet.name,
                    modifier = Modifier.clickable(
                        onClick = {
                            onNavigateToPetProfile(pet.id)
                        }
                    )
                )
            }

            item {
                PetProfile(
                    petImage = painterResource(id = R.drawable.ic_add_circle),
                    name = stringResource(R.string.add_pet),
                    modifier = Modifier.clickable(
                        onClick = onNavigateToAddPet
                    )
                )
            }
        }
    }
}

@Composable
private fun PetProfile(petImage: Painter, name: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(Dimen.spaceMedium)
            .clip(RoundedCornerShape(Dimen.petIconCornerRadiusPercent))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimen.spaceMedium),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = petImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(Dimen.petIconSize)
                    .clip(RoundedCornerShape(100))
            )
            Spacer(modifier = Modifier.height(Dimen.spaceMedium))
            Text(text = name)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onNavigateToSettings: () -> Unit,
    onNavigateToUserProfile: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(text = stringResource(R.string.pets))
        },
        navigationIcon = {
            IconButton(
                onClick = onNavigateToUserProfile
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = null
                )
            }
        },
        actions = {
            IconButton(
                onClick = onNavigateToSettings

            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = null
                )
            }
        }
    )
}