package com.helios.spaceweather.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Helios is a dark-mode-first product: the color scheme is force-dark regardless of the
 * system setting, because the brutalist visual language and the OLED true-black surfaces
 * are core to the design, not a theme variant.
 *
 * Only the neutral roles are themed here. The three Kp accents are deliberately NOT mapped
 * onto Material roles (primary/secondary/…) so they can't leak into chrome; consumers read
 * them directly (or via [com.helios.spaceweather.core.util.KpThreatLevel]) for data only.
 */
private val HeliosDarkColorScheme = darkColorScheme(
    background = TrueBlack,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = OnSurfaceMuted,
    outline = Outline,
    outlineVariant = Outline,
    // A restrained neutral "primary" so default Material components stay monochrome.
    primary = OnSurface,
    onPrimary = TrueBlack,
)

@Composable
fun HeliosTheme(
    // Accepted for API symmetry but ignored: Helios is always dark by design.
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = TrueBlack.toArgb()
            window.navigationBarColor = TrueBlack.toArgb()
            val insets = WindowCompat.getInsetsController(window, view)
            // Light content on dark bars.
            insets.isAppearanceLightStatusBars = false
            insets.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = HeliosDarkColorScheme,
        typography = HeliosTypography,
        shapes = HeliosShapes,
        content = content,
    )
}
