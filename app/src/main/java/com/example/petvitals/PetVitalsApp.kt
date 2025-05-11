package com.example.petvitals

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.petvitals.ui.screens.add_pet.AddPetScreen
import com.example.petvitals.ui.screens.log_in.SignInScreen
import com.example.petvitals.ui.screens.pet_profile.PetProfileScreen
import com.example.petvitals.ui.screens.pets.PetsScreen
import com.example.petvitals.ui.screens.sign_up.SignUpScreen
import com.example.petvitals.ui.screens.splash.SplashScreen
import com.example.petvitals.ui.screens.user_profile.UserProfileScreen
import com.example.petvitals.ui.theme.PetVitalsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetVitalsApp(modifier: Modifier = Modifier) {
    PetVitalsTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = Splash,
            modifier = modifier
        ) {
            composable<LogIn> {
                SignInScreen(navigateTo = { route -> navController.navigate(route = route) })
            }
            composable<SignUp> {
                SignUpScreen(
                    navigateTo = { route -> navController.navigate(route = route) },
                    onPopBackStack = { navController.popBackStack() }
                )
            }
            composable<Pets> {
                PetsScreen(
                    restartApp = { route -> navController.navigate(route = route) },
                    onNavigateToAddPet = { navController.navigate(route = AddPet) },
                    onNavigateToPetProfile = { petId -> navController.navigate(route = PetProfile(petId)) },
                    onNavigateToSettings = {  },
                    onNavigateToUserProfile = { navController.navigate(route = UserProfile) }
                )
            }
            composable<Splash> {
                SplashScreen(navigateTo = { route -> navController.navigate(route = route) })
            }
            composable<UserProfile> {
                UserProfileScreen()
            }
            composable<AddPet> {
                AddPetScreen(
                    navigateToPets = { navController.navigate(route = Pets) },
                    onPopBackStack = { navController.popBackStack() }
                )
            }
            composable<PetProfile> { backStackEntry ->
                val petProfile: PetProfile = backStackEntry.toRoute()
                PetProfileScreen(
                    petProfile = petProfile,
                    onPopBackStack = { navController.popBackStack() },
                    onNavigateToEditPet = {},
                    onNavigateToPets = { navController.navigate(route = Pets) }
                )
            }
        }
    }
}