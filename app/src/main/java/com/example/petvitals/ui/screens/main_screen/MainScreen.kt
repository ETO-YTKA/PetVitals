package com.example.petvitals.ui.screens.main_screen

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.petvitals.AddEditPet
import com.example.petvitals.CreateRecord
import com.example.petvitals.PetProfile
import com.example.petvitals.Pets
import com.example.petvitals.R
import com.example.petvitals.Records
import com.example.petvitals.UserProfile
import com.example.petvitals.ui.screens.add_pet.AddEditPetScreen
import com.example.petvitals.ui.screens.create_record.CreateRecordScreen
import com.example.petvitals.ui.screens.pet_profile.PetProfileScreen
import com.example.petvitals.ui.screens.pets.PetsScreen
import com.example.petvitals.ui.screens.records.RecordsScreen
import com.example.petvitals.ui.screens.user_profile.UserProfileScreen

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
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Pets,
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            composable<Pets> {
                PetsScreen(
                    onNavigateToSplash = onNavigateToSplash,
                    onNavigateToAddPet = { navController.navigate(route = AddEditPet()) },
                    onNavigateToPetProfile = { petId ->
                        navController.navigate(
                            route = PetProfile(
                                petId
                            )
                        )
                    },
                    onNavigateToSettings = { },
                    onNavigateToUserProfile = { navController.navigate(route = UserProfile) }
                )
            }
            composable<UserProfile> {
                UserProfileScreen(onPopBackStack = { navController.popBackStack() })
            }
            composable<AddEditPet> { backStackEntry ->
                val addEditPet: AddEditPet = backStackEntry.toRoute()
                AddEditPetScreen(
                    addEditPet = addEditPet,
                    navigateToPets = { navController.navigate(route = Pets) },
                    onPopBackStack = { navController.popBackStack() }
                )
            }
            composable<PetProfile> { backStackEntry ->
                val petProfile: PetProfile = backStackEntry.toRoute()
                PetProfileScreen(
                    petProfile = petProfile,
                    onPopBackStack = { navController.popBackStack() },
                    onNavigateToEditPet = { petId -> navController.navigate(route = AddEditPet(petId)) },
                    onNavigateToPets = { navController.navigate(route = Pets) }
                )
            }
            composable<CreateRecord> {
                CreateRecordScreen(onPopBackStack = { navController.popBackStack() })
            }
            composable<Records> {
                RecordsScreen(
                    onNavigateToCreateRecord = { navController.navigate(route = CreateRecord) }
                )
            }
        }
    }
}

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
            onClick = onNavigateToPets
        )
        NavigationBarItem(
            selected = hierarchy?.any { it.hasRoute(Records::class) } == true,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_description),
                    contentDescription = stringResource(R.string.records)
                )
            },
            onClick = onNavigateToRecords
        )
    }
}