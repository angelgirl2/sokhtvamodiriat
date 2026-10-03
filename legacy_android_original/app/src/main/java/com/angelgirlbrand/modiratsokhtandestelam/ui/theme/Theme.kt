package com.angelgirlbrand.modiratsokhtandestelam.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.angelgirlbrand.modiratsokhtandestelam.security.AppThemeColor
import com.angelgirlbrand.modiratsokhtandestelam.security.DarkModePref

// Dedicated Sky Blue Light Color Scheme
val SkyBlueLightColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    onPrimary = Color.White,
    primaryContainer = SkyBlueContainer,
    onPrimaryContainer = SkyBlueOnContainer,
    secondary = PetrolDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF0F172A),
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF1E293B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

// Dedicated Sky Blue Dark Color Scheme
val SkyBlueDarkColorScheme = darkColorScheme(
    primary = SkyBluePrimaryDark,
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFBBE9FF),
    secondary = Color(0xFF93C5FD),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFF1F5F9),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF8FAFC),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155)
)

@Composable
fun MyApplicationTheme(
    appThemeColor: AppThemeColor = AppThemeColor.SKY_BLUE,
    darkModePref: DarkModePref = DarkModePref.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (darkModePref) {
        DarkModePref.SYSTEM -> isSystemDark
        DarkModePref.LIGHT -> false
        DarkModePref.DARK -> true
    }

    val colorScheme = if (isDark) SkyBlueDarkColorScheme else SkyBlueLightColorScheme

    // Force RTL layout direction for Persian UI
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
