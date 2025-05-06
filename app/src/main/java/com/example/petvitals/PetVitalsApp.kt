package com.example.petvitals

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.petvitals.ui.screens.add_pet.AddPetScreen
import com.example.petvitals.ui.screens.log_in.SignInScreen
import com.example.petvitals.ui.screens.pets.PetsScreen
import com.example.petvitals.ui.screens.sign_up.SignUpScreen
import com.example.petvitals.ui.screens.splash.SplashScreen
import com.example.petvitals.ui.screens.user_profile.UserProfileScreen
import com.example.petvitals.ui.theme.PetVitalsTheme
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetVitalsApp(modifier: Modifier = Modifier) {
    PetVitalsTheme {
        val navController = rememberNavController()
        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val route = currentBackStackEntry?.destination?.route
        val routesWithoutAppBar = listOf(
            SignUp.toString().substringBefore('@'),
            LogIn.toString().substringBefore('@'),
            Splash.toString().substringBefore('@')
        )
        var topBarTitle by remember { mutableStateOf<String>("PetVitals") }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (!routesWithoutAppBar.contains(route)) {
                    TopBar(
                        title = topBarTitle,
                        onActionClick = {  },
                        onNavigationClick = { navController.navigate(route = UserProfile) }
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
                        topAppBarTitle = { composableTitle -> topBarTitle = composableTitle },
                        onAddPetClick = { navController.navigate(route = AddPet) }
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    onActionClick: () -> Unit,
    onNavigationClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1
            )
        },
        actions = {
            IconButton(
                onClick = onActionClick
            ) {
                Icon(painterResource(R.drawable.settings_24dp), contentDescription = null)
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onNavigationClick
            ) {
                Icon(painterResource(R.drawable.person_24dp), contentDescription = null)
            }
        }
    )
}
@Serializable
object LogIn

@Serializable
object SignUp

@Serializable
object Splash

@Serializable
object Pets

@Serializable
object UserProfile

@Serializable
object AddPet
