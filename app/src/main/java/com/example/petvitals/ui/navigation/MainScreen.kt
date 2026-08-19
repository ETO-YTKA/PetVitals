package com.example.petvitals.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.petvitals.R
import com.example.petvitals.ui.screens.joinpet.JoinPetScreen
import com.example.petvitals.ui.screens.managefood.ManageFoodScreen
import com.example.petvitals.ui.screens.managemedication.ManageMedicationScreen
import com.example.petvitals.ui.screens.managepet.ManagePetScreen
import com.example.petvitals.ui.screens.managerecord.ManageRecordScreen
import com.example.petvitals.ui.screens.petprofile.PetProfileScreen
import com.example.petvitals.ui.screens.pets.PetsAction
import com.example.petvitals.ui.screens.pets.PetsScreen
import com.example.petvitals.ui.screens.pets.PetsViewModel
import com.example.petvitals.ui.screens.records.RecordsScreen
import com.example.petvitals.ui.screens.sharepet.SharePetScreen
import com.example.petvitals.ui.screens.userprofile.UserProfileScreen

@Composable
fun MainAppScreen(
    onNavigateToSplash: () -> Unit
) {
    val navController = rememberNavController()
    val hierarchy = navController.currentBackStackEntryAsState().value?.destination?.hierarchy

    Scaffold(
        bottomBar = {
            BottomBar(
                hierarchy = hierarchy,
                onNavigateToPets = { navController.navigate(route = Pets) },
                onNavigateToRecords = { navController.navigate(route = Records) }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Pets,
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            composable<Pets> { backStackEntry ->
                val viewModel: PetsViewModel = hiltViewModel()
                val refreshRequested by backStackEntry.savedStateHandle
                    .getStateFlow(PETS_REFRESH_REQUESTED_KEY, false)
                    .collectAsState()

                LaunchedEffect(refreshRequested) {
                    if (refreshRequested) {
                        viewModel.onAction(PetsAction.RefreshPets)
                        backStackEntry.savedStateHandle[PETS_REFRESH_REQUESTED_KEY] = false
                    }
                }

                PetsScreen(
                    viewModel = viewModel,
                    onNavigateToSplash = onNavigateToSplash,
                    onNavigateToAddPet = { navController.navigate(route = AddEditPet()) },
                    onNavigateToPetProfile = { petId ->
                        navController.navigate(route = PetProfile(petId))
                    },
                    onNavigateToUserProfile = { navController.navigate(route = UserProfile) },
                    onNavigateToJoinPet = { navController.navigate(route = JoinPet) }
                )
            }

            composable<UserProfile> {
                UserProfileScreen(onPopBackStack = { navController.popBackStack() })
            }

            composable<AddEditPet> { backStackEntry ->
                val addEditPet: AddEditPet = backStackEntry.toRoute()
                ManagePetScreen(
                    addEditPet = addEditPet,
                    onNavigateToPets = { navController.navigate(route = Pets) },
                    onPopBackStack = { navController.popBackStack() }
                )
            }

            composable<PetProfile> { backStackEntry ->
                val petProfile: PetProfile = backStackEntry.toRoute()
                PetProfileScreen(
                    petProfile = petProfile,
                    onNavigateToPets = { navController.popBackStack() },
                    onNavigateToEditPet = { petId -> navController.navigate(route = AddEditPet(petId)) },
                    onNavigateToSharePet = { petId -> navController.navigate(route = SharePet(petId)) },
                    onNavigateToAddEditMedication = { addEditMedication ->
                        navController.navigate(route = AddEditMedication(
                            addEditMedication.petId,
                            addEditMedication.medicationId
                        ))
                    },
                    onNavigateToAddEditFood = {  addEditFood ->
                        navController.navigate(route = AddEditFood(
                            addEditFood.petId,
                            addEditFood.foodId
                        ))
                    },
                )
            }

            composable<AddEditRecord> { backStackEntry ->
                val addEditRecord: AddEditRecord = backStackEntry.toRoute()
                ManageRecordScreen(
                    addEditRecord = addEditRecord,
                    onPopBackStack = { navController.popBackStack() }
                )
            }

            composable<Records> {
                RecordsScreen(
                    onNavigateToAddEditRecord = { recordId -> navController.navigate(route = AddEditRecord(recordId)) },
                    onNavigateToProfile = { navController.navigate(route = UserProfile) },
                    onNavigateToPetProfile = { petId -> navController.navigate(route = PetProfile(petId)) }
                )
            }

            composable<SharePet> { backStackEntry ->
                val sharePet: SharePet = backStackEntry.toRoute()
                SharePetScreen(
                    petId = sharePet.petId,
                    onPopBackStack = { navController.popBackStack() }
                )
            }

            composable<AddEditMedication> { backStackEntry ->
                val addEditMedication: AddEditMedication = backStackEntry.toRoute()
                ManageMedicationScreen(
                    addEditMedication = addEditMedication,
                    onPopBackStack = { navController.popBackStack() }
                )
            }

            composable<AddEditFood> { backStackEntry ->
                val addEditFood: AddEditFood = backStackEntry.toRoute()
                ManageFoodScreen(
                    addEditFood = addEditFood,
                    onPopBackStack = { navController.popBackStack() }
                )
            }

            composable<JoinPet> {
                JoinPetScreen(
                    onPopBackStack = { navController.popBackStack() },
                    onPetJoined = {
                        if (navController.currentDestination?.hasRoute(JoinPet::class) == true) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set(PETS_REFRESH_REQUESTED_KEY, true)
                            navController.popBackStack()
                        }
                    }
                )
            }
        }
    }
}

private const val PETS_REFRESH_REQUESTED_KEY = "pets_refresh_requested"

@Composable
private fun BottomBar(
    hierarchy: Sequence<NavDestination>?,
    onNavigateToPets: () -> Unit,
    onNavigateToRecords: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = hierarchy?.any { it.hasRoute(Pets::class) } == true,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_pets),
                    contentDescription = stringResource(R.string.pets)
                )
            },
            onClick = onNavigateToPets,
            label = { Text(stringResource(R.string.pets)) }
        )
        NavigationBarItem(
            selected = hierarchy?.any { it.hasRoute(Records::class) } == true,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_description),
                    contentDescription = stringResource(R.string.records)
                )
            },
            onClick = onNavigateToRecords,
            label = { Text(stringResource(R.string.records)) }
        )
    }
}
