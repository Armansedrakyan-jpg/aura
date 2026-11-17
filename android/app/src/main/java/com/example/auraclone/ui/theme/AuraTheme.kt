package com.example.auraclone.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AuraDarkColorScheme = darkColorScheme(
    primary = AuraColors.PlasmaViolet,
    onPrimary = Color.White,
    secondary = AuraColors.ElectricCyan,
    onSecondary = AuraColors.VoidBlack,
    tertiary = AuraColors.HyperMagenta,
    onTertiary = Color.White,
    background = AuraColors.VoidBlack,
    onBackground = AuraColors.TextPrimary,
    surface = AuraColors.ObsidianGlass,
    onSurface = AuraColors.TextPrimary,
    surfaceVariant = AuraColors.ElevatedGlass,
    onSurfaceVariant = AuraColors.TextSecondary,
    error = AuraColors.CrimsonAlert,
    onError = Color.White,
    outline = AuraColors.GlassBorderSoft
)

@Composable
fun AuraTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = AuraDarkColorScheme,
        typography = AuraTypography,
        content = content
    )
}
