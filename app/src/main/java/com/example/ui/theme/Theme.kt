package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SazfloyColorScheme = darkColorScheme(
    primary = SazfloyAccentPurple,
    onPrimary = Color.Black,
    primaryContainer = SazfloySecondaryPurple,
    onPrimaryContainer = Color.White,
    secondary = SazfloySecondaryPurple,
    onSecondary = SazfloyTextPrimary,
    secondaryContainer = SazfloySurfaceVariant,
    onSecondaryContainer = SazfloyAccentPurple,
    tertiary = SazfloyPink,
    onTertiary = Color.White,
    background = SazfloyBackground,
    onBackground = SazfloyTextPrimary,
    surface = SazfloySurface,
    onSurface = SazfloyTextPrimary,
    surfaceVariant = SazfloySurfaceVariant,
    onSurfaceVariant = SazfloyTextSecondary,
    outline = SazfloyBorder,
    outlineVariant = SazfloyDivider
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = SazfloyBackground.toArgb()
                window.navigationBarColor = SazfloyBackground.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = SazfloyColorScheme,
        typography = Typography,
        content = content
    )
}
