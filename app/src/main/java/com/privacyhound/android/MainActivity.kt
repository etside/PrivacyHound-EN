package com.privacyhound.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.privacyhound.android.ui.screens.DashboardScreen
import com.privacyhound.android.ui.screens.GuideScreen
import com.privacyhound.android.ui.screens.HistoryScreen
import com.privacyhound.android.ui.theme.PrivacyHoundTheme

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN_HISTORY = "open_history"
        const val EXTRA_FOCUS_PACKAGE = "focus_pkg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrivacyHoundTheme {
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    if (intent.getBooleanExtra(EXTRA_OPEN_HISTORY, false)) {
                        navController.navigate("history") {
                            launchSingleTop = true
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "dashboard",
                    modifier = Modifier
                ) {
                    composable("dashboard") {
                        DashboardScreen(
                            onOpenHistory = { navController.navigate("history") },
                            onOpenGuide = { navController.navigate("guide") }
                        )
                    }
                    composable("history") {
                        HistoryScreen(onBack = { navController.popBackStack() })
                    }
                    composable("guide") {
                        GuideScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
