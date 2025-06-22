package com.example.petvitals.ui.screens.pets

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetSpecies
import com.example.petvitals.data.repository.pet_permissions.PermissionLevel
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
//    LaunchedEffect(Unit) {
//        val token = Firebase.messaging.token.await()
//        Log.d("FCM token:", token)
//    }

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
        //RequestNotificationPermission()

        val pets = uiState.pets

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshPets() }
        ) {
            //show text if empty
            if (pets.isEmpty() && !uiState.isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_pets_added_yet),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = Dimen.spaceMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium)
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

@Composable
fun PetListItem(
    pet: Pet,
    modifier: Modifier = Modifier
) {
    val isShared = pet.currentUserPermission != PermissionLevel.OWNER

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (isShared) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        }
    ) {
        Row(
            modifier = Modifier.padding(Dimen.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //Pet avatar
            val fallbackRes = when (pet.species) {
                PetSpecies.CAT -> R.drawable.ic_cat
                PetSpecies.DOG -> R.drawable.ic_dog
            }
            val image = pet.avatar?.let { decodeBase64ToImage(it) }

            AsyncImage(
                model = image,
                contentDescription = pet.name,
                contentScale = ContentScale.Crop,
                fallback = painterResource(fallbackRes),
                modifier = Modifier
                    .size(64.dp)
                    .then(if (image != null) Modifier.clip(CircleShape) else Modifier)
            )

            Spacer(modifier = Modifier.width(Dimen.spaceMedium))

            //Name, species, and permission Level
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(pet.species.stringRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isShared) {
                    PermissionRow(permissionLevel = pet.currentUserPermission)
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = stringResource(R.string.view_profile),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PermissionRow(permissionLevel: PermissionLevel, modifier: Modifier = Modifier) {
    val (icon, text) = when (permissionLevel) {
        PermissionLevel.EDITOR -> painterResource(R.drawable.ic_edit) to stringResource(R.string.permission_level_editor)
        PermissionLevel.VIEWER -> painterResource(R.drawable.ic_rounded_visibility) to stringResource(R.string.permission_level_viewer)
        else -> return
    }

    Row(
        modifier = modifier.padding(top = Dimen.spaceSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimen.spaceSmall)
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

@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val context = LocalContext.current
        var hasNotificationPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        // Create the permission launcher
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                hasNotificationPermission = isGranted
                if (!isGranted) {
                    // Optional: Show a Snackbar or message explaining why the
                    // permission is important if the user denied it.
                }
            }
        )

        LaunchedEffect(key1 = true) {
            if (!hasNotificationPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}