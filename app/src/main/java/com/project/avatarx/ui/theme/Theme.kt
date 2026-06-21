package com.project.avatarx.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val AvatarXColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = NightBlack,
    primaryContainer = DeepNavy,
    onPrimaryContainer = IceBlue,
    secondary = SteelBlue,
    onSecondary = NightBlack,
    secondaryContainer = GraphiteSurface,
    onSecondaryContainer = SilverHighlight,
    tertiary = MintSuccess,
    onTertiary = NightBlack,
    background = NightBlack,
    onBackground = SilverHighlight,
    surface = OnyxSurface,
    onSurface = SilverHighlight,
    surfaceVariant = GraphiteSurface,
    onSurfaceVariant = CloudGray,
    error = CoralAlert,
    onError = NightBlack,
    outline = DimGray,
    outlineVariant = StoneGray
)

// Sharp, industrial shapes for ATLAS
private val AvatarXShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

@Composable
fun AvatarXTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AvatarXColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AvatarXTypography,
        shapes = AvatarXShapes,
        content = content
    )
}