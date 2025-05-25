package com.example.petvitals.ui.screens.pet_profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.PetProfile
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet.PetSpecies
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.utils.decodeBase64ToImage

@Composable
fun PetProfileScreen(
    petProfile: PetProfile,
    onPopBackStack: () -> Unit,
    onNavigateToEditPet: (String) -> Unit,
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
        topBar = {
            TopBar(
                title = { Text(uiState.name) },
                navigationIcon = {
                    CustomIconButton(
                        onClick = onPopBackStack,
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                },
                actions = {
                    CustomIconButton(
                        onClick = { onNavigateToEditPet(petProfile.petId) },
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = stringResource(R.string.edit_pet)
                    )
                    CustomIconButton(
                        onClick = {
                            viewModel.deletePet(petProfile.petId)
                            onPopBackStack()
                        },
                        painter = painterResource(R.drawable.ic_delete_forever),
                        contentDescription = stringResource(R.string.delete_pet)
                    )
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val painterRes = if (uiState.pet.species == PetSpecies.CAT) R.drawable.ic_cat
                else R.drawable.ic_dog
            val image = uiState.pet.imageString?.let { remember { decodeBase64ToImage(it) } }
            val imageModifier = Modifier
                .size(Dimen.petImageProfile)
                .then(if (image != null) Modifier.clip(CircleShape) else Modifier)

            AsyncImage(
                model = image,
                contentDescription = uiState.pet.name,
                contentScale = ContentScale.Crop,
                fallback = painterResource(painterRes),
                modifier = imageModifier
            )
        }

        Spacer(modifier = Modifier.height(Dimen.spaceHuge))
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Dimen.spaceMedium),
                verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium)
            ) {
                Text(text = uiState.name)
                Text(text = uiState.birthDate)
            }
        }
    }
}