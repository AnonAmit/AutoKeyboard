package com.autokeyboard.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════════════════
// Extended color scheme for custom tokens not in Material3
// ═══════════════════════════════════════════════════════════════
@Immutable
data class ExtendedColors(
    val keyTiles: Color = KeyTiles,
    val sheetBackground: Color = SheetBackground,
    val gradientStart: Color = GradientStart,
    val gradientEnd: Color = GradientEnd,
    val shimmerBase: Color = ShimmerBase,
    val shimmerHighlight: Color = ShimmerHighlight,
    val primaryDim: Color = PrimaryDim,
    val primaryFixedDim: Color = PrimaryFixedDim,
    val secondaryDim: Color = SecondaryDim,
    val errorDim: Color = ErrorDim,
)

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }

// ═══════════════════════════════════════════════════════════════
// Material3 Dark Color Scheme
// ═══════════════════════════════════════════════════════════════
private val NeonNocturneDarkScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary = InversePrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = SurfaceTint,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    outline = Outline,
    outlineVariant = OutlineVariant,
    surfaceBright = SurfaceBright,
    surfaceDim = SurfaceDim,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainerLowest = SurfaceContainerLowest,
)

// OLED scheme: pure black backgrounds for battery saving
private val OledDarkScheme = NeonNocturneDarkScheme.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF080815),
    surfaceContainer = Color(0xFF0E0E20),
)

// ═══════════════════════════════════════════════════════════════
// Theme Enum
// ═══════════════════════════════════════════════════════════════
enum class AppTheme {
    SYSTEM, DARK, OLED
}

// ═══════════════════════════════════════════════════════════════
// Composable Theme
// ═══════════════════════════════════════════════════════════════
@Composable
fun AutoKeyboardTheme(
    appTheme: AppTheme = AppTheme.DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.OLED -> OledDarkScheme
        AppTheme.DARK -> NeonNocturneDarkScheme
        AppTheme.SYSTEM -> NeonNocturneDarkScheme // Always dark for this app
    }

    val extendedColors = ExtendedColors()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AutoKeyboardTypography,
            shapes = AutoKeyboardShapes,
            content = content
        )
    }
}

// Convenience accessor
object AutoKeyboardThemeDefaults {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
