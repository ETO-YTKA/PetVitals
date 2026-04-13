package com.example.petvitals.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.petvitals.ui.screens.login.LoginScreen
import com.example.petvitals.ui.screens.passwordreset.PasswordResetScreen
import com.example.petvitals.ui.screens.signup.SignUpScreen
import com.example.petvitals.ui.screens.splash.SplashScreen
import com.example.petvitals.ui.theme.PetVitalsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetVitalsApp() {
    PetVitalsTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = Splash
        ) {
            composable<LogIn> {
                LoginScreen(
                    navigateToSignUp = { navController.navigate(route = SignUp) },
                    navigateToSplash = { navController.navigate(route = Splash) },
                    navigateToPasswordReset = { navController.navigate(route = PasswordReset) }
                )
            }
            composable<SignUp> {
                SignUpScreen(
                    onNavigateToLogIn = { navController.navigate(route = LogIn) },
                    onPopBackStack = { navController.popBackStack() }
                )
            }
            composable<Splash> {
                SplashScreen(
                    onNavigateToMainApp = { navController.navigate(route = MainApp) },
                    onNavigateToLogIn = { navController.navigate(route = LogIn) }
                )
            }
            composable<MainApp> {
                MainAppScreen(onNavigateToSplash = { navController.navigate(route = Splash) })
            }
            composable<PasswordReset> {
                PasswordResetScreen(onPopBackStack = { navController.popBackStack() })
            }
        }
    }
}