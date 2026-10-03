package com.angelgirlbrand.modiratsokhtandestelam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.angelgirlbrand.modiratsokhtandestelam.security.AppThemeColor
import com.angelgirlbrand.modiratsokhtandestelam.security.DarkModePref
import com.angelgirlbrand.modiratsokhtandestelam.ui.screens.LockScreen
import com.angelgirlbrand.modiratsokhtandestelam.ui.screens.MainScreen
import com.angelgirlbrand.modiratsokhtandestelam.ui.screens.SplashScreen
import com.angelgirlbrand.modiratsokhtandestelam.ui.theme.MyApplicationTheme
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.BaleServiceViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.BaleServiceViewModelFactory
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.FuelViewModelFactory

enum class AppDestination {
    SPLASH,
    LOCK,
    MAIN
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as FuelApplication
        val repository = app.repository
        val securityManager = app.securityManager
        val notificationHelper = app.notificationHelper

        setContent {
            val fuelViewModel: FuelViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = FuelViewModelFactory(repository, notificationHelper)
            )
            val baleViewModel: BaleServiceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = BaleServiceViewModelFactory(repository, securityManager)
            )

            var currentDestination by remember { mutableStateOf(AppDestination.SPLASH) }
            var currentThemeColor by remember { mutableStateOf(securityManager.getSelectedThemeColor()) }
            var currentDarkModePref by remember { mutableStateOf(securityManager.getDarkModePref()) }

            MyApplicationTheme(
                appThemeColor = currentThemeColor,
                darkModePref = currentDarkModePref
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (currentDestination) {
                        AppDestination.SPLASH -> {
                            SplashScreen(
                                securityManager = securityManager,
                                onNavigateToNext = {
                                    if (securityManager.isPinEnabled() && securityManager.isAppLocked) {
                                        currentDestination = AppDestination.LOCK
                                    } else {
                                        currentDestination = AppDestination.MAIN
                                    }
                                }
                            )
                        }

                        AppDestination.LOCK -> {
                            BackHandler {
                                finish()
                            }
                            LockScreen(
                                securityManager = securityManager,
                                onUnlocked = {
                                    currentDestination = AppDestination.MAIN
                                }
                            )
                        }

                        AppDestination.MAIN -> {
                            MainScreen(
                                fuelViewModel = fuelViewModel,
                                baleViewModel = baleViewModel,
                                securityManager = securityManager,
                                currentThemeColor = currentThemeColor,
                                currentDarkModePref = currentDarkModePref,
                                onThemeColorChanged = { newColor ->
                                    securityManager.setSelectedThemeColor(newColor)
                                    currentThemeColor = newColor
                                },
                                onDarkModePrefChanged = { newMode ->
                                    securityManager.setDarkModePref(newMode)
                                    currentDarkModePref = newMode
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
