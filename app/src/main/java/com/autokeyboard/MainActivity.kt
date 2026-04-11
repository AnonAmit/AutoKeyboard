package com.autokeyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.autokeyboard.data.local.PreferencesManager
import com.autokeyboard.ui.navigation.NavGraph
import com.autokeyboard.ui.navigation.Routes
import com.autokeyboard.ui.theme.AutoKeyboardTheme
import com.autokeyboard.ui.theme.Background
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main activity — shown when user opens the app from launcher.
 * Handles onboarding flow and IME enablement guidance.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            val onboardingDone = preferencesManager.onboardingComplete.first()
            val startDest = if (onboardingDone) Routes.SETTINGS else Routes.ONBOARDING

            setContent {
                AutoKeyboardTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Background)
                    ) {
                        val navController = rememberNavController()
                        NavGraph(
                            navController = navController,
                            startDestination = startDest,
                            onOnboardingComplete = {
                                lifecycleScope.launch {
                                    preferencesManager.setOnboardingComplete()
                                }
                                // Guide user to enable the keyboard
                                openIMESettings()
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Opens the Android IME settings so the user can enable AutoKeyboard.
     */
    private fun openIMESettings() {
        try {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        } catch (_: Exception) { }
    }

    /**
     * Checks if AutoKeyboard is currently the active IME.
     */
    private fun isKeyboardEnabled(): Boolean {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.enabledInputMethodList.any {
            it.packageName == packageName
        }
    }
}
