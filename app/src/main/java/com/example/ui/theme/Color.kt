package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// Sleek Coffee App Color Palette (Extracted from reference design)
// Rich Espresso, Warm Caramel Latte, Froth Cream & Ivory
// =========================================================================

// Coffee Foundation Constants
val CoffeeCaramel = Color(0xFFC69A72)          // Signature warm caramel / latte tan
val CoffeeCaramelDark = Color(0xFFB3835B)      // Deeper caramel
val CoffeeEspresso = Color(0xFF54382B)         // Deep roasted espresso brown
val CoffeeEspressoDark = Color(0xFF382319)     // Rich dark roast / text primary
val CoffeeMocha = Color(0xFF7A543E)            // Medium roasted mocha
val CoffeeCream = Color(0xFFFAF5EE)            // Soft latte froth cream background
val CoffeeSurfaceWarm = Color(0xFFF5ECE2)      // Warm latte card / container
val CoffeeBorderSoft = Color(0xFFE6D7C8)       // Soft sand / latte border

// Light Theme Palette
val ShopBackgroundLight = Color(0xFFFAF5EE)        // Warm cream / milk froth background: #FAF5EE
val ShopSurfaceLight = Color(0xFFFFFFFF)           // Clean card background: #FFFFFF
val ShopSurfaceElevatedLight = Color(0xFFF5ECE2)   // Warm container & chip background: #F5ECE2
val ShopCardLight = Color(0xFFFFFFFF)              // Card background: #FFFFFF
val ShopBorderLight = Color(0xFFE6D7C8)            // Warm coffee borders: #E6D7C8
val ShopInputBackground = Color(0xFFF6EFE7)        // Input background: #F6EFE7
val ShopSuccessBackground = Color(0xFFF3E8DB)      // Warm latte froth tint: #F3E8DB
val ShopWarningBackground = Color(0xFFFDF3E7)      // Warm amber / cinnamon tint
val ShopErrorBackground = Color(0xFFFBECEC)        // Soft muted red tint

val TextPrimaryLight = Color(0xFF362217)           // Deep espresso text: #362217
val TextSecondaryLight = Color(0xFF7D6556)         // Warm mocha secondary text: #7D6556
val TextMutedLight = Color(0xFFA89587)             // Muted warm sand text

// Brand Accent Colors
val ShopPrimary = Color(0xFF54382B)                // Deep Espresso Brown: #54382B
val ShopPrimaryVariant = Color(0xFF382319)         // Rich Dark Roast: #382319
val ShopSecondary = Color(0xFFC69A72)              // Warm Caramel Latte: #C69A72
val ShopSecondaryVariant = Color(0xFFB3835B)       // Golden Caramel

val ShopCyanAccent = Color(0xFFC69A72)             // Caramel accent highlight
val ShopEmeraldAccent = Color(0xFF5A7E55)          // Warm olive/roast green for positive metrics/profit
val ShopAmberAccent = Color(0xFFD48B4A)            // Warm cinnamon / amber
val ShopRoseAccent = Color(0xFFC95346)             // Terracotta / roasted chili red

// Dark Theme Palette (Deep Dark Roast & Warm Caramel)
val ShopBackgroundDark = Color(0xFF1A120C)         // Deep dark roasted coffee bean
val ShopSurfaceDark = Color(0xFF281C14)            // Dark espresso bean card
val ShopSurfaceElevatedDark = Color(0xFF36251B)    // Elevated mocha container
val ShopCardDark = Color(0xFF281C14)
val ShopBorderDark = Color(0xFF4A3427)
val ShopBorderHighlight = Color(0xFF634635)

val TextPrimaryDark = Color(0xFFFDF8F3)            // Froth white
val TextSecondaryDark = Color(0xFFD7C5B5)          // Warm latte foam text
val TextMutedDark = Color(0xFF9E8A7B)

// Gradients (Espresso & Caramel)
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(ShopPrimary, ShopSecondary)    // Deep Espresso to Warm Caramel
)

val GlowGradient = Brush.linearGradient(
    colors = listOf(
        ShopSecondary.copy(alpha = 0.20f),
        ShopPrimary.copy(alpha = 0.10f),
        Color.Transparent
    )
)

val CardGlassGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFFAF5EE)
    )
)
