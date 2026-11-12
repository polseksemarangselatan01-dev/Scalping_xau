package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GoldScalpingDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF101010),
    primaryContainer = GoldContainer,
    onPrimaryContainer = OnGoldContainer,
    secondary = BuyGreen,
    onSecondary = Color.Black,
    secondaryContainer = BuyGreenContainer,
    onSecondaryContainer = Color(0xFFA7FFEB),
    tertiary = SellRed,
    onTertiary = Color.White,
    tertiaryContainer = SellRedContainer,
    onTertiaryContainer = Color(0xFFFFCDD2),
    background = NavyBlack,
    onBackground = TextWhite,
    surface = NavySurface,
    onSurface = TextWhite,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = NavyBorder,
    outlineVariant = DividerColor
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to true for professional trading terminal
    dynamicColor: Boolean = false, // Keep branded luxury gold theme
    content: @Composable () -> Unit
) {
    val colorScheme = GoldScalpingDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = NavyBlack.toArgb()
            window.navigationBarColor = NavyBlack.toArgb()
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars = false
            windowInsetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
