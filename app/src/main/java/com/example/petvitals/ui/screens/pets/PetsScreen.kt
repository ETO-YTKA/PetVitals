package com.example.petvitals.ui.screens.pets

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetSpecies
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.utils.decodeBase64ToImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetsScreen(
    onNavigateToSplash: () -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetProfile: (String) -> Unit,
    onNavigateToUserProfile: () -> Unit,
    viewModel: PetsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.initialize(onNavigateToSplash) }

    ScreenLayout(
        topBar = {
            TopBar(
                title = { Text(stringResource(R.string.pets)) },
                navigationIcon = {
                    CustomIconButton(
                        onClick = onNavigateToUserProfile,
                        painter = painterResource(R.drawable.ic_person),
                        contentDescription = stringResource(R.string.user_profile)
                    )
                },
                actions = {
                    CustomIconButton(
                        onClick = onNavigateToAddPet,
                        painter = painterResource(R.drawable.ic_add_circle),
                        contentDescription = stringResource(R.string.add_pet)
                    )
                }
            )
        }
    ) {
        val pets = uiState.pets

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshPets() }
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = Dimen.petIconSize),
                modifier = Modifier.fillMaxSize()
            ) {
                items(pets.size) { index ->
                    val pet = pets[index]
                    PetProfile(
                        pet = pet,
                        modifier = Modifier.clickable(
                            onClick = { onNavigateToPetProfile(pet.id) }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PetProfile(pet: Pet, modifier: Modifier = Modifier) {
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
            val fallbackRes = if (pet.species == PetSpecies.CAT) R.drawable.ic_cat
                else R.drawable.ic_dog
            val image = pet.imageString?.let { decodeBase64ToImage(it) }
            val imageModifier = Modifier
                .size(Dimen.petIconSize)
                .then(if (image != null) Modifier.clip(CircleShape) else Modifier)

            AsyncImage(
                model = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                fallback = painterResource(fallbackRes),
                modifier = imageModifier
            )

            Spacer(modifier = Modifier.height(Dimen.spaceMedium))

            Text(text = pet.name)
        }
    }
}