package com.aarav.geowav.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val AppTypography = Typography(
    displayLarge = Typography().displayLarge.copy(fontFamily = manrope),
    displayMedium = Typography().displayMedium.copy(fontFamily = manrope),
    displaySmall = Typography().displaySmall.copy(fontFamily = manrope),

    headlineLarge = Typography().headlineLarge.copy(fontFamily = manrope),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = manrope),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = manrope),

    titleLarge = Typography().titleLarge.copy(fontFamily = manrope),
    titleMedium = Typography().titleMedium.copy(fontFamily = manrope),
    titleSmall = Typography().titleSmall.copy(fontFamily = manrope),

    bodyLarge = Typography().bodyLarge.copy(fontFamily = sora),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = sora),
    bodySmall = Typography().bodySmall.copy(fontFamily = sora),

    labelLarge = Typography().labelLarge.copy(fontFamily = sora),
    labelMedium = Typography().labelMedium.copy(fontFamily = sora),
    labelSmall = Typography().labelSmall.copy(fontFamily = sora),
)

