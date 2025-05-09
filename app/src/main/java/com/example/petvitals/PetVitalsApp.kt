package com.example.petvitals

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
        val context = LocalContext.current

        var showAppBars by rememberSaveable { mutableStateOf(false) }
        var currentRoute by rememberSaveable { mutableStateOf<Routes?>(null) }
        var title by rememberSaveable { mutableStateOf("") }

        navController.addOnDestinationChangedListener { _, destination, arguments ->

            when (destination.route?.substringBefore('/')) {
                LogIn::class.java.name -> {
                    showAppBars = false
                    currentRoute = Routes.LogIn
                    title = ""
                }
                SignUp::class.java.name -> {
                    showAppBars = false
                    currentRoute = Routes.SignUp
                    title = ""
                }
                Splash::class.java.name -> {
                    showAppBars = false
                    currentRoute = Routes.Splash
                    title = ""
                }
                Pets::class.java.name -> {
                    showAppBars = true
                    currentRoute = Routes.Pets
                    title = context.getString(R.string.pets)
                }
                UserProfile::class.java.name -> {
                    showAppBars = true
                    currentRoute = Routes.UserProfile
                    title = context.getString(R.string.profile)
                }
                AddPet::class.java.name -> {
                    showAppBars = true
                    currentRoute = Routes.AddPet
                    title = ""
                }
                PetProfile::class.java.name -> {
                    showAppBars = true
                    currentRoute = Routes.PetProfile
                    title = ""
                }
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (showAppBars) {
                    TopBar(
                        title = title,
                        onNavigateToSettings = {  },
                        onNavigateToProfile = { navController.navigate(route = UserProfile) },
                        popBackStack = { navController.popBackStack() },
                        currentRoute = currentRoute
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Splash,
                modifier = modifier.padding(innerPadding)
            ) {
                composable<LogIn> {
                    SignInScreen(navigateTo = { route -> navController.navigate(route = route) })
                }
                composable<SignUp> {
                    SignUpScreen(navigateTo = { route -> navController.navigate(route = route) })
                }
                composable<Pets> {
                    PetsScreen(
                        restartApp = { route -> navController.navigate(route = route) },
                        onNavigateToAddPet = { navController.navigate(route = AddPet) },
                        onNavigateToPetProfile = { petId -> navController.navigate(route = PetProfile(petId)) }
                    )
                }
                composable<Splash> {
                    SplashScreen(navigateTo = { route -> navController.navigate(route = route) })
                }
                composable<UserProfile> {
                    UserProfileScreen()
                }
                composable<AddPet> {
                    AddPetScreen(navigateToPets = { navController.navigate(route = Pets) })
                }
                composable<PetProfile> { backStackEntry ->
                    val petProfile: PetProfile = backStackEntry.toRoute()
                    PetProfileScreen(petProfile = petProfile)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    popBackStack: () -> Unit,
    currentRoute: Routes? = null
) {
    Log.d("TopBar", "currentRoute: $currentRoute")

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1
            )
        },
        actions = {
            when(currentRoute) {
                Routes.PetProfile -> {
                    IconButton(
                        onClick = { }
                    ) {
                        Icon(painterResource(R.drawable.ic_edit), contentDescription = null)
                    }
                }
                else -> {
                    IconButton(
                        onClick = onNavigateToSettings
                    ) {
                        Icon(painterResource(R.drawable.settings_24dp), contentDescription = null)
                    }
                }
            }
        },
        navigationIcon = {
            when(currentRoute) {
                Routes.PetProfile -> {
                    IconButton(
                        onClick = popBackStack
                    ) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = null)
                    }
                }
                else -> {
                    IconButton(
                        onClick = onNavigateToProfile
                    ) {
                        Icon(painterResource(R.drawable.person_24dp), contentDescription = null)
                    }
                }
            }
        }
    )
}