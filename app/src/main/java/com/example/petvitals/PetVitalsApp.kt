package com.example.petvitals

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetVitalsApp(modifier: Modifier = Modifier) {
    PetVitalsTheme {
        val navController = rememberNavController()
        val currentBackStackEntry by navController.currentBackStackEntryAsState()

        var currentRoute by remember { mutableStateOf<AppRoute?>(null) }

        LaunchedEffect(currentBackStackEntry) {
            if (currentBackStackEntry?.destination?.route != null) {
                currentRoute = try {
                    currentBackStackEntry?.toRoute<AppRoute>()
                } catch (e: IllegalArgumentException) {
                    Log.e("PetVitalsApp", "Failed to deserialize route: ${currentBackStackEntry?.destination?.route}", e)
                    null
                }
            } else if (currentBackStackEntry == null && navController.graph.startDestinationRoute != null) {
                currentRoute = null
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                currentRoute?.let { route ->
                    if (route.hasTopBottomBar) {
                        TopBar(
                            title = route.title?.let { stringResource(id = it) } ?: "",
                            onNavigateToSettings = {  },
                            onNavigateToProfile = { navController.navigate(route = UserProfile) },
                            popBackStack = { navController.popBackStack() },
                            currentRoute = currentRoute
                        )
                    }
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
    currentRoute: AppRoute? = null
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1
            )
        },
        actions = {
            when(currentRoute) {
                is PetProfile -> {
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
                is PetProfile -> {
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

@Serializable
object LogIn : AppRoute {
    override val hasTopBottomBar: Boolean = false
    override val title: Int? = null
}

@Serializable
object SignUp : AppRoute {
    override val hasTopBottomBar: Boolean = false
    override val title: Int? = null
}

@Serializable
object Splash : AppRoute {
    override val hasTopBottomBar: Boolean = false
    override val title: Int? = null
}

@Serializable
object Pets : AppRoute {
    override val hasTopBottomBar: Boolean = true
    override val title: Int? = R.string.pets
}

@Serializable
object UserProfile : AppRoute {
    override val hasTopBottomBar: Boolean = true
    override val title: Int? = R.string.profile
}

@Serializable
object AddPet : AppRoute {
    override val hasTopBottomBar: Boolean = true
    override val title: Int? = R.string.add_your_pet
}

@Serializable
data class PetProfile(val petId: String) : AppRoute {
    override val hasTopBottomBar: Boolean = true
    override val title: Int? = null
}

@Serializable
sealed interface AppRoute {

    val hasTopBottomBar: Boolean
    @get:StringRes
    val title: Int?
}