package com.hologen.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hologen.app.ui.theme.HologenColors
import com.hologen.app.ui.theme.HologenMetrics

enum class HologenTab(
    val title: String,
    val icon: ImageVector
) {
    Scan("Scan", Icons.Outlined.Home),
    Workspace("Workspace", Icons.Outlined.ViewCarousel),
    Settings("Settings", Icons.Outlined.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HologenNavigation(
    selectedTab: HologenTab,
    onTabSelected: (HologenTab) -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(HologenColors.Background.primary),
        bottomBar = {
            NavigationBar(
                containerColor = HologenColors.Background.card,
                contentColor = HologenColors.Text.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                HologenTab.entries.forEach { tab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (selectedTab == tab) 
                                    HologenColors.Accent.mint 
                                else 
                                    HologenColors.Text.secondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (selectedTab == tab) 
                                    HologenColors.Accent.mint 
                                else 
                                    HologenColors.Text.secondary
                            )
                        },
                        selected = selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = HologenColors.Accent.mint.copy(alpha = 0.2f),
                            selectedIconColor = HologenColors.Accent.mint,
                            selectedTextColor = HologenColors.Accent.mint,
                            unselectedIconColor = HologenColors.Text.secondary,
                            unselectedTextColor = HologenColors.Text.secondary
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        content(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}