package com.hologen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.hologen.app.ui.navigation.HologenNavigation
import com.hologen.app.ui.navigation.HologenTab
import com.hologen.app.ui.screens.HistoryScreen
import com.hologen.app.ui.screens.LoginScreen
import com.hologen.app.ui.screens.ScanScreen
import com.hologen.app.ui.screens.SettingsScreen
import com.hologen.app.ui.screens.WorkspaceScreen
import com.hologen.app.ui.theme.HologenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HologenTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    var selectedTab by remember { mutableStateOf(HologenTab.Scan) }
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }

    if (!isLoggedIn) {
        // Show Login Screen
        LoginScreen(
            onLoginClick = { 
                // TODO: Implement actual login logic here
                isLoggedIn = true 
            },
            onRegisterClick = { 
                // TODO: Implement actual register logic here
                isLoggedIn = true 
            }
        )
    } else {
        // Show Main App
        HologenNavigation(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        ) { modifier ->
            when (selectedTab) {
                HologenTab.Scan -> ScanScreen(modifier = modifier)
                HologenTab.Workspace -> WorkspaceScreen(modifier = modifier)
                HologenTab.History -> HistoryScreen(modifier = modifier)
                HologenTab.Settings -> SettingsScreen(modifier = modifier)
            }
        }
    }
}