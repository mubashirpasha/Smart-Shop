package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = ShopSecondary,                         // #C69A72 Warm Caramel Accent
    onPrimary = Color(0xFF281C14),
    primaryContainer = ShopPrimary,                  // #54382B Espresso
    onPrimaryContainer = Color(0xFFFDF8F3),
    secondary = ShopSecondary,                       // #C69A72
    onSecondary = Color(0xFF281C14),
    secondaryContainer = ShopPrimaryVariant,
    onSecondaryContainer = Color(0xFFFDF8F3),
    tertiary = ShopAmberAccent,                      // #D48B4A Cinnamon Amber
    onTertiary = Color.White,
    background = ShopBackgroundDark,                 // #1A120C Roasted Bean Dark
    onBackground = TextPrimaryDark,                  // #FDF8F3 Froth White
    surface = ShopSurfaceDark,                       // #281C14 Dark Espresso Card
    onSurface = TextPrimaryDark,                     // #FDF8F3
    surfaceVariant = ShopSurfaceElevatedDark,        // #36251B Elevated Mocha
    onSurfaceVariant = TextSecondaryDark,            // #D7C5B5 Warm Latte Foam
    outline = ShopBorderDark,                        // #4A3427 Dark Roast Border
    outlineVariant = ShopBorderHighlight,
    error = ShopRoseAccent,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ShopPrimary,                           // #54382B Deep Roasted Espresso Brown
    onPrimary = Color.White,
    primaryContainer = ShopSuccessBackground,        // #F3E8DB Warm Latte Froth Cream Tint
    onPrimaryContainer = ShopPrimary,
    secondary = ShopSecondary,                       // #C69A72 Warm Caramel Latte
    onSecondary = Color.White,
    secondaryContainer = ShopSurfaceElevatedLight,   // #F5ECE2 Warm Container Background
    onSecondaryContainer = ShopPrimary,
    tertiary = ShopCyanAccent,                       // #C69A72 Caramel Tan Accent
    onTertiary = Color.White,
    background = ShopBackgroundLight,                // #FAF5EE Warm Latte Froth Background
    onBackground = TextPrimaryLight,                 // #362217 Deep Espresso Text
    surface = ShopSurfaceLight,                      // #FFFFFF Crisp Card Background
    onSurface = TextPrimaryLight,                    // #362217 Deep Espresso Text
    surfaceVariant = ShopSurfaceElevatedLight,       // #F5ECE2 Warm Container & Input Background
    onSurfaceVariant = TextSecondaryLight,           // #7D6556 Warm Mocha Secondary Text
    outline = ShopBorderLight,                       // #E6D7C8 Soft Latte Border
    outlineVariant = ShopBorderLight,                // #E6D7C8
    error = ShopRoseAccent,                          // Terracotta Red
    onError = Color.White,
    errorContainer = ShopErrorBackground,
    onErrorContainer = Color(0xFF991B1B)
)

/**
 * 3D-inspired rounded curvatures for smooth lighting and specular highlights.
 */
val ShopFlowShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Global Theme accessor for custom 3D elevations and gradients.
 */
object ShopFlowTheme {
    val elevations: ShopFlowElevations
        @Composable
        @ReadOnlyComposable
        get() = LocalShopFlowElevations.current

    val gradients: ShopFlowGradients
        @Composable
        @ReadOnlyComposable
        get() = LocalShopFlowGradients.current
}

@Composable
fun ShopFlowTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val elevations = if (darkTheme) DarkShopFlowElevations else LightShopFlowElevations
    val gradients = if (darkTheme) DarkShopFlowGradients else LightShopFlowGradients

    CompositionLocalProvider(
        LocalShopFlowElevations provides elevations,
        LocalShopFlowGradients provides gradients
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = ShopFlowShapes,
            typography = Typography,
            content = content
        )
    }
}
