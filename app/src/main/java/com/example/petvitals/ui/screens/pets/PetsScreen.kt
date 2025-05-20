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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.R
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBarProfileAddPet
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.utils.decodeBase64ToImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetsScreen(
    onNavigateToSplash: () -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetProfile: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToUserProfile: () -> Unit,
    viewModel: PetsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.initialize(onNavigateToSplash) }

    ScreenLayout(
        topBar = {
            TopBarProfileAddPet(
                title = stringResource(R.string.pets),
                onNavigateToAddPet = onNavigateToAddPet,
                onNavigateToUserProfile = onNavigateToUserProfile,
            )
        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = onNavigateToAddPet,
//                modifier = Modifier
//                    .padding(Dimen.spaceMedium)
//                    .size(Dimen.fabSize)
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_add_circle),
//                    contentDescription = null,
//                    modifier = Modifier.size(Dimen.fabIconSize)
//                )
//            }
//        }
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
                    val petImage = pet.imageString?.let { decodeBase64ToImage(it) }
                        ?: if (pet.species == "Cat") painterResource(id = R.drawable.ic_cat)
                        else painterResource(id = R.drawable.ic_dog)

                    PetProfile(
                        petImage = petImage,
                        name = pet.name,
                        modifier = Modifier.clickable(
                            onClick = {
                                onNavigateToPetProfile(pet.id)
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PetProfile(petImage: Any, name: String, modifier: Modifier = Modifier) {
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
            if (petImage is Painter) {
                Image(
                    painter = petImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(Dimen.petIconSize)
                        .padding(15.dp)
                )
            } else {
                AsyncImage(
                    model = petImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(Dimen.petIconSize)
                        .clip(RoundedCornerShape(100))
                )
            }
            Spacer(modifier = Modifier.height(Dimen.spaceMedium))
            Text(text = name)
        }
    }
}