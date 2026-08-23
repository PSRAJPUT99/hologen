package com.hologen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.hologen.app.ui.navigation.HologenNavigation
import com.hologen.app.ui.navigation.HologenTab
import com.hologen.app.ui.screens.ScanScreen
import com.hologen.app.ui.screens.SettingsScreen
import com.hologen.app.ui.theme.HologenColors
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
            HologenTab.Settings -> SettingsScreen(modifier = modifier)
            
            // Placeholders for tabs we haven't built yet (to prevent build errors)
            HologenTab.Workspace -> PlaceholderScreen("Workspace", modifier)
            HologenTab.History -> PlaceholderScreen("History", modifier)
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HologenColors.Background.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$name coming soon",
            style = MaterialTheme.typography.bodyLarge,
            color = HologenColors.Text.secondary,
            textAlign = TextAlign.Center
        )
    }
}