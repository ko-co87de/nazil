package com.nazil.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Amber,
    onPrimary = Indigo900,
    primaryContainer = AmberLight,
    onPrimaryContainer = Indigo900,
    secondary = Rose,
    onSecondary = WarmWhite,
    secondaryContainer = RoseLight,
    onSecondaryContainer = Indigo950,
    tertiary = Indigo500,
    onTertiary = WarmWhite,
    background = WarmWhite,
    onBackground = Indigo900,
    surface = SurfaceLight,
    onSurface = Indigo900,
    surfaceVariant = Indigo50,
    onSurfaceVariant = Indigo700,
    outline = Muted,
    error = RoseDark,
    onError = WarmWhite,
)

private val DarkColors = darkColorScheme(
    primary = Amber,
    onPrimary = Indigo950,
    primaryContainer = AmberDark,
    onPrimaryContainer = WarmWhite,
    secondary = RoseLight,
    onSecondary = Indigo950,
    secondaryContainer = RoseDark,
    onSecondaryContainer = WarmWhite,
    tertiary = Indigo500,
    onTertiary = WarmWhite,
    background = Indigo950,
    onBackground = OnDark,
    surface = SurfaceDark,
    onSurface = OnDark,
    surfaceVariant = Indigo900,
    onSurfaceVariant = Indigo100,
    outline = Muted,
    error = RoseLight,
    onError = Indigo950,
)

@Composable
fun NazilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = NazilTypography,
        content = content,
    )
}
