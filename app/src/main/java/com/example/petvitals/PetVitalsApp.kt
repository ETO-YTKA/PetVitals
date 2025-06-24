package com.example.petvitals

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.petvitals.ui.screens.log_in.SignInScreen
import com.example.petvitals.ui.screens.main_screen.MainAppScreen
import com.example.petvitals.ui.screens.password_reset.PasswordResetScreen
import com.example.petvitals.ui.screens.sign_up.SignUpScreen
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
                SignInScreen(
                    onNavigateToSignUp = { navController.navigate(route = SignUp) },
                    onNavigateToSplash = { navController.navigate(route = Splash) },
                    onNavigateToPasswordReset = { navController.navigate(route = PasswordReset) }
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