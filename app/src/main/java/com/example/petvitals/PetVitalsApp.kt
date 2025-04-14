package com.example.petvitals

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.petvitals.ui.screens.log_in.SignInScreen
import com.example.petvitals.ui.screens.hallo.HalloScreen
import com.example.petvitals.ui.screens.sign_up.SignUpScreen
import com.example.petvitals.ui.screens.splash.SplashScreen
import com.example.petvitals.ui.theme.PetVitalsTheme
import kotlinx.serialization.Serializable

@Composable
fun PetVitalsApp(modifier: Modifier = Modifier) {
    PetVitalsTheme {
        val navController = rememberNavController()
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
                composable<Hallo> {
                    HalloScreen(restartApp = { route -> navController.navigate(route = route)})
                }
                composable<Splash> {
                    SplashScreen(navigateTo = { route -> navController.navigate(route = route) })
                }
            }
        }
    }
}

@Serializable
object LogIn

@Serializable
object SignUp

@Serializable
object Splash

@Serializable
object Hallo