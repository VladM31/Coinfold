package com.vm.coinfold.app.shared.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Purple = Color(0xFF7C4DFF)
private val PurpleDark = Color(0xFF5E35D6)
private val PurpleLight = Color(0xFFB39DFF)

/** Light theme is mostly white with purple accents. */
private val LightColors: ColorScheme = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFE9FF),
    onPrimaryContainer = Color(0xFF2A0F6B),
    secondary = PurpleDark,
    background = Color.White,
    onBackground = Color(0xFF1B1A22),
    surface = Color.White,
    onSurface = Color(0xFF1B1A22),
    surfaceVariant = Color(0xFFF6F3FF),
    onSurfaceVariant = Color(0xFF5F5B70),
    outlineVariant = Color(0xFFE6E0F5),
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = PurpleLight,
    onPrimary = Color(0xFF2A0F6B),
    primaryContainer = Color(0xFF3A2780),
    onPrimaryContainer = Color(0xFFE9E1FF),
    secondary = Purple,
    background = Color(0xFF121016),
    onBackground = Color(0xFFE8E4F2),
    surface = Color(0xFF121016),
    onSurface = Color(0xFFE8E4F2),
    surfaceVariant = Color(0xFF1E1A28),
    onSurfaceVariant = Color(0xFFB4AEC6),
    outlineVariant = Color(0xFF332E44),
)

/** Green used for the "remaining" ring segment and income amounts. */
val IncomeGreen = Color(0xFF2EB872)

@Composable
fun CoinfoldTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
