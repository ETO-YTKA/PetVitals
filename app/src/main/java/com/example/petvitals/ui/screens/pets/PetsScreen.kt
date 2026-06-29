package com.example.petvitals.ui.screens.pets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.R
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.PetSpecies
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.ui.theme.PetVitalsTheme
import com.example.petvitals.ui.utils.decodeBase64ToImage

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

    PetsScreenContent(
        uiState = uiState,
        onAction = { action -> viewModel.onAction(action) },
        onNavigateToAddPet = onNavigateToAddPet,
        onNavigateToPetProfile = onNavigateToPetProfile,
        onNavigateToUserProfile = onNavigateToUserProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetsScreenContent(
    uiState: PetsUiState,
    onAction: (PetsAction) -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetProfile: (String) -> Unit,
    onNavigateToUserProfile: () -> Unit,
) {
    Scaffold(
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
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimen.Screen.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val pets = uiState.pets

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { onAction(PetsAction.RefreshPets) }
            ) {
                when {
                    uiState.errorMessage != null -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.errorMessage,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onAction(PetsAction.RefreshPets) },
                                modifier = Modifier.widthIn(min = 200.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.refresh)
                                )
                            }
                        }
                    }

                    pets.isEmpty() && !uiState.isRefreshing -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.no_pets_added_yet),
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onNavigateToAddPet,
                                modifier = Modifier.widthIn(min = 200.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.add_pet)
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = pets,
                                key = { pet -> pet.id }
                            ) { pet ->
                                PetListItem(
                                    pet = pet,
                                    modifier = Modifier.clickable { onNavigateToPetProfile(pet.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PetListItem(
    pet: Pet,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //Pet avatar
            val image = pet.avatar?.let { decodeBase64ToImage(it) }

            if (image == null) {
                val fallbackRes = when (pet.species) {
                    PetSpecies.CAT -> R.drawable.ic_cat
                    PetSpecies.DOG -> R.drawable.ic_dog
                }
                Icon(
                    painter = painterResource(fallbackRes),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            } else {
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
            }

            //Name, species, and permission Level
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                pet.breed?.let {
                    Text(
                        text = pet.breed,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (pet.currentUserPermission != PermissionLevel.OWNER) {

                    PermissionChip(permissionLevel = pet.currentUserPermission)
                }
            }

            //Navigation Icon
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = stringResource(R.string.view_profile)
            )
        }
    }
}

@Composable
private fun PermissionChip(permissionLevel: PermissionLevel) {
    val icon = when (permissionLevel) {
        PermissionLevel.OWNER -> painterResource(R.drawable.ic_person)
        PermissionLevel.EDITOR -> painterResource(R.drawable.ic_edit)
        PermissionLevel.VIEWER -> painterResource(R.drawable.ic_rounded_visibility)
    }

    val text = when (permissionLevel) {
        PermissionLevel.OWNER -> stringResource(R.string.permission_level_owner)
        PermissionLevel.EDITOR -> stringResource(R.string.permission_level_editor)
        PermissionLevel.VIEWER -> stringResource(R.string.permission_level_viewer)
    }

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.secondary
            )

            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PetsScreenContentEmptyPreview() {
    PetVitalsTheme {
        PetsScreenContent(
            uiState = PetsUiState(),
            onAction = {},
            onNavigateToAddPet = {},
            onNavigateToPetProfile = {},
            onNavigateToUserProfile = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun PetsScreenContentListPreview() {
    PetVitalsTheme {
        PetsScreenContent(
            uiState = PetsUiState(
                pets = listOf(
                    Pet(
                        id = "owner-cat",
                        name = "Mochi",
                        breed = "Siamese",
                        species = PetSpecies.CAT,
                        currentUserPermission = PermissionLevel.OWNER
                    ),
                    Pet(
                        id = "shared-dog",
                        name = "Biscuit",
                        breed = "Golden Retriever",
                        species = PetSpecies.DOG,
                        currentUserPermission = PermissionLevel.EDITOR
                    )
                )
            ),
            onAction = {},
            onNavigateToAddPet = {},
            onNavigateToPetProfile = {},
            onNavigateToUserProfile = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun PetsScreenContentErrorPreview() {
    PetVitalsTheme {
        PetsScreenContent(
            uiState = PetsUiState(errorMessage = "Error loading pets"),
            onAction = {},
            onNavigateToAddPet = {},
            onNavigateToPetProfile = {},
            onNavigateToUserProfile = {}
        )
    }
}