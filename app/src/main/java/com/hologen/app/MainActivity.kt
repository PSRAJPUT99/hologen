package com.hologen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hologen.app.ui.navigation.HologenNavigation
import com.hologen.app.ui.navigation.HologenTab
import com.hologen.app.ui.screens.HistoryScreen
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