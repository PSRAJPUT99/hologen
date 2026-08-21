package com.hologen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
        enableEdgeToEdge()
        setContent {
            HologenTheme {
                Surface(modifier = Modifier.fillMaxSize()) { HologenApp() }
            }
        }
    }
}

@Composable
private fun HologenApp() {
    var selectedTab by remember { mutableStateOf(HologenTab.Scan) }

    HologenNavigation(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it }
    ) { contentModifier ->
        when (selectedTab) {
            HologenTab.Scan -> ScanScreen(modifier = contentModifier)
            HologenTab.Workspace -> WorkspaceScreen(modifier = contentModifier)
            HologenTab.History -> HistoryScreen(modifier = contentModifier)
            HologenTab.Settings -> SettingsScreen(modifier = contentModifier)
        }
    }
}
