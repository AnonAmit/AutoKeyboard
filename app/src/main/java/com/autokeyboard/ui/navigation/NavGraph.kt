package com.autokeyboard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.autokeyboard.ui.onboarding.OnboardingScreen
import com.autokeyboard.ui.settings.SettingsScreen

/**
 * Navigation routes for the main activity (not the keyboard IME).
 * The keyboard UI lives inside KeyboardService, not in NavGraph.
 */
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val SETTINGS = "settings"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    onOnboardingComplete: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onSkip = {
                    onOnboardingComplete()
                    navController.navigate(Routes.SETTINGS) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onComplete = {
                    onOnboardingComplete()
                    navController.navigate(Routes.SETTINGS) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
            SettingsScreen(
                onBack = { 
                    if (!navController.popBackStack()) {
                        activity?.finish()
                    }
                }
            )
        }

        composable(Routes.HOME) {
            // Home/landing screen — redirect to settings for now
            SettingsScreen(
                onBack = { /* No back from home */ }
            )
        }
    }
}
