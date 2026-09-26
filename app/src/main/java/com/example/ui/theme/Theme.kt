package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CatProjectAgentDarkColorScheme = darkColorScheme(
    primary = CatAmberPrimary,
    onPrimary = CatOnAmber,
    primaryContainer = CatAmberContainer,
    onPrimaryContainer = CatAmberHover,
    secondary = CatAmberHover,
    onSecondary = CatOnAmber,
    secondaryContainer = CatSurfaceVariant,
    onSecondaryContainer = CatTextPrimary,
    background = CatBackground,
    onBackground = CatTextPrimary,
    surface = CatSurface,
    onSurface = CatTextPrimary,
    surfaceVariant = CatSurfaceVariant,
    onSurfaceVariant = CatTextSecondary,
    outline = CatBorder,
    outlineVariant = Color(0xFF1E2638)
)

val CatProjectAgentLightColorScheme = lightColorScheme(
    primary = CatAmberDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = CatAmberDark,
    onSecondary = Color.White,
    secondaryContainer = SurfaceVariantLight,
    onSecondaryContainer = TextPrimaryLight,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = CardBorderLight,
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun CatProjectAgentTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CatProjectAgentDarkColorScheme else CatProjectAgentLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
