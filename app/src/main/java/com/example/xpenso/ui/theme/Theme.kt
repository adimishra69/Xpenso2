package com.example.xpenso.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. This holds our custom budget status colors
data class BudgetColors(
    val blueBg: Color, val blueTitle: Color, val blueDesc: Color,
    val greenBg: Color, val greenTitle: Color, val greenDesc: Color,
    val yellowBg: Color, val yellowTitle: Color, val yellowDesc: Color,
    val orangeBg: Color, val orangeTitle: Color, val orangeDesc: Color
)

// 2. Define the Dark Mode palette
private val DarkBudgetColors = BudgetColors(
    blueBg = Color(0xFF1E2A38), blueTitle = Color(0xFF90CAF9), blueDesc = Color.White,
    greenBg = Color(0xFF1F3D2B), greenTitle = Color(0xFF4CAF50), greenDesc = Color.White,
    yellowBg = Color(0xFF3A2F0B), yellowTitle = Color(0xFFFFD54F), yellowDesc = Color(0xFFFFECB3),
    orangeBg = Color(0xFF3A2A1A), orangeTitle = Color(0xFFFFB74D), orangeDesc = Color.White
)

// 3. Define the Light Mode palette
private val LightBudgetColors = BudgetColors(
    blueBg = Color(0xFFE3F2FD), blueTitle = Color(0xFF1565C0), blueDesc = Color(0xFF1A237E),
    greenBg = Color(0xFFE6F4EA), greenTitle = Color(0xFF2E7D32), greenDesc = Color(0xFF388E3C),
    yellowBg = Color(0xFFFFECB3), yellowTitle = Color(0xFFF9A825), yellowDesc = Color(0xFF5D4037),
    orangeBg = Color(0xFFFEF0C7), orangeTitle = Color(0xFFE65100), orangeDesc = Color(0xFF3E2723)
)

// 4. Create a "Local" provider so screens can find these colors
val LocalBudgetColors = staticCompositionLocalOf { LightBudgetColors }
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8B93F8),
    onPrimary = Color(0xFF070B14),

//    primaryContainer = Color(0xFF141A33),
    primaryContainer = Color(0xFF1A2242),
    onPrimaryContainer = Color(0xFFE0E7FF),

    secondary = Color(0xFF5DD6A4),
    tertiary = Color(0xFF67E8F9),

    background = Color(0xFF060A10),

    surface = Color(0xFF0E131B),

    surfaceVariant = Color(0xFF121826),

    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1F2937)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4338CA),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = Color(0xFF059669),
    tertiary = Color(0xFF0891B2),
    background = Color(0xFFF6F8FB),  // softer
    surface = Color(0xFFFAFBFF),

    onBackground = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    outline = Color(0xFFB8C2CC),
    outlineVariant = Color(0xFFE2E8F0)
)

private val ModernDarkColorScheme = darkColorScheme(
    // Luxury Graphite + Bronze Theme
    // Grey is the main color, bronze/brown is the supporting accent.

    // Primary accent (bronze)
    primary = Color(0xFFB08968),          // Warm bronze
    onPrimary = Color(0xFF1A1410),

    primaryContainer = Color(0xFF3A2C22), // Deep bronze container
    onPrimaryContainer = Color(0xFFF5E8DD),

    secondary = Color(0xFFC4A484),        // Soft bronze
    tertiary = Color(0xFFD6B89B),         // Champagne bronze

    // Main neutral surfaces (grey-focused)
    background = Color(0xFF121416),       // Deep graphite grey
    onBackground = Color(0xFFF8FAFC),

    surface = Color(0xFF121416),          // Same as background
    onSurface = Color(0xFFF8FAFC),

    surfaceVariant = Color(0xFF1E2227),   // Elevated grey surface
    onSurfaceVariant = Color(0xFFD1D5DB),

    // Borders
    outline = Color(0xFF4B5563),
    outlineVariant = Color(0xFF2D3339)
)

private val ModernLightColorScheme = lightColorScheme(
    // Luxury Grey + Bronze Theme (Light)

    // Primary accent (bronze)
    primary = Color(0xFF8B5E3C),          // Rich bronze brown
    onPrimary = Color.White,

    primaryContainer = Color(0xFFF3E7DC), // Soft warm beige
    onPrimaryContainer = Color(0xFF3E2A1E),

    secondary = Color(0xFFA97C5A),
    tertiary = Color(0xFFC4A484),

    // Main neutral surfaces (grey-focused)
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF111827),

    surface = Color(0xFFF7F8FA),          // Same as background
    onSurface = Color(0xFF111827),

    surfaceVariant = Color(0xFFEFF1F4),
    onSurfaceVariant = Color(0xFF4B5563),

    // Borders
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB)
)

@Composable
fun XpensoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),

    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = true,
    colorStyle: Int = 0,
    content: @Composable () -> Unit
) {
//    val context = LocalContext.current
//
//    val colorScheme =
//        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//
//            // SYSTEM COLORS (Material You)
//            if (darkTheme)
//                dynamicDarkColorScheme(context)
//            else
//                dynamicLightColorScheme(context)
//
//        } else {
//
//            // APP COLORS (BLUE THEME)
//            if (darkTheme)
//                DarkColorScheme
//            else
//                LightColorScheme
//        }

    val colorScheme =
        when (colorStyle) {
            1 -> if (darkTheme) ModernDarkColorScheme else ModernLightColorScheme
            else -> if (darkTheme) DarkColorScheme else LightColorScheme
        }
    // 5. Select which custom set to provide
    val budgetColors = if (darkTheme) DarkBudgetColors else LightBudgetColors

    UpdateSystemBars(darkTheme)

    // 6. Wrap the theme so all children can use budgetColors
    CompositionLocalProvider(LocalBudgetColors provides budgetColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun UpdateSystemBars(darkTheme: Boolean) {
    val view = LocalView.current

    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view)
            .isAppearanceLightStatusBars = !darkTheme
    }
}