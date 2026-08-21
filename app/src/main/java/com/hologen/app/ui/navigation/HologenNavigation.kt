package com.hologen.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hologen.app.ui.theme.HologenColors
import com.hologen.app.ui.theme.HologenMetrics

enum class HologenTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Scan("Scan", Icons.Outlined.Home),
    Workspace("Workspace", Icons.Outlined.Layers),
    History("History", Icons.Outlined.AccessTime),
    Settings("Settings", Icons.Outlined.Settings)
}

@Composable
fun HologenNavigation(
    selectedTab: HologenTab,
    onTabSelected: (HologenTab) -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(HologenColors.Background.primary)) {
        Box(modifier = Modifier.weight(1f)) {
            content(Modifier.fillMaxSize())
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HologenColors.Background.card)
                .navigationBarsPadding()
                .padding(horizontal = HologenMetrics.space8, vertical = HologenMetrics.space8),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HologenTab.entries.forEach { tab ->
                TabItem(
                    tab = tab,
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Box(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@Composable
private fun TabItem(tab: HologenTab, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(HologenMetrics.buttonRadius))
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null,
                onClick = onClick
            )
            .padding(vertical = HologenMetrics.space4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HologenMetrics.space4)
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = if (selected) HologenColors.Accent.mint else HologenColors.Text.secondary
        )
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) HologenColors.Accent.mint else HologenColors.Text.secondary
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.42f)
                .clip(RoundedCornerShape(HologenMetrics.buttonRadius))
                .background(if (selected) HologenColors.Accent.mint else HologenColors.Background.card)
                .padding(vertical = HologenMetrics.space4)
        )
    }
}
