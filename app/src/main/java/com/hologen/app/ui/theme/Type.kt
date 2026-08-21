package com.hologen.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object HologenTypography {
	val screenTitle = TextStyle(
		color = HologenColors.Text.primary,
		fontSize = 32.sp,
		lineHeight = 38.sp,
		fontWeight = FontWeight.Bold
	)
	val cardHeading = TextStyle(
		color = HologenColors.Text.primary,
		fontSize = 26.sp,
		lineHeight = 32.sp,
		fontWeight = FontWeight.Bold
	)
	val body = TextStyle(
		color = HologenColors.Text.secondary,
		fontSize = 16.sp,
		lineHeight = 24.sp,
		fontWeight = FontWeight.Normal
	)
	val tabLabel = TextStyle(
		color = HologenColors.Text.secondary,
		fontSize = 15.sp,
		lineHeight = 20.sp,
		fontWeight = FontWeight.Normal
	)
}

val Typography = Typography(
	headlineSmall = HologenTypography.screenTitle,
	titleLarge = HologenTypography.cardHeading,
	titleMedium = HologenTypography.body.copy(fontSize = 16.sp, lineHeight = 22.sp),
	bodyLarge = HologenTypography.body,
	bodyMedium = HologenTypography.body.copy(fontSize = 14.sp, lineHeight = 20.sp),
	labelLarge = HologenTypography.tabLabel
)
