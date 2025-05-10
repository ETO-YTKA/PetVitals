package com.example.petvitals.ui.screens.pet_profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.PetProfile
import com.example.petvitals.R
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.theme.Dimen

@Composable
fun PetProfileScreen(
    petProfile: PetProfile,
    onPopBackStack: () -> Unit,
    onNavigateToEditScreen: () -> Unit,
    viewModel: PetProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getPetData(petProfile.petId)
    }

    ScreenLayout(
        modifier = Modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
        topBar = { TopBar(
            onPopBackStack = onPopBackStack,
            onNavigateToEditScreen = onNavigateToEditScreen,
            onDeleteClick = {
                viewModel.deletePet(petProfile.petId)
                onPopBackStack()
            }
        ) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.splash),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(Dimen.petImageProfile)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(Dimen.spaceMedium))
            Text(text = uiState.name, style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(Dimen.spaceHuge))
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Dimen.spaceMedium),
                verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium)
            ) {
                Text(text = uiState.species)
                Text(text = uiState.birthDate)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onPopBackStack: () -> Unit,
    onNavigateToEditScreen: () -> Unit,
    onDeleteClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "",
                maxLines = 1
            )
        },
        actions = {
            IconButton(
                onClick = onNavigateToEditScreen
            ) {
                Icon(painterResource(R.drawable.ic_edit), contentDescription = null)
            }
            IconButton(
                onClick = onDeleteClick
            ) {
                Icon(painterResource(R.drawable.ic_delete_forever), contentDescription = null)
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onPopBackStack

            ) {
                Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = null)
            }
        }
    )
}