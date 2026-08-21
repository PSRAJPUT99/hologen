package com.hologen.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
	headlineSmall = TextStyle(fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.Medium),
	titleLarge = TextStyle(fontSize = 21.sp, lineHeight = 26.sp, fontWeight = FontWeight.Medium),
	titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
	bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 23.sp),
	bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
	labelLarge = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
)
