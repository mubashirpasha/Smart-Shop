package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Custom 3D gradients system for the futuristic coffee aesthetic:
 * Espresso, Caramel Latte, Cream Froth, and Specular Light reflections.
 */
@Immutable
data class ShopFlowGradients(
    val primary3D: Brush,
    val secondary3D: Brush,
    val heroCard3D: Brush,
    val cardSurface3D: Brush,
    val container3D: Brush,
    val beveledBorder3D: Brush,
    val interactiveBevel3D: Brush,
    val ambientGlow3D: Brush,
    val pressed3D: Brush,
    val darkCardSurface3D: Brush
)

val LightShopFlowGradients = ShopFlowGradients(
    // 3D Primary Button: Specular Espresso at top-left to Warm Caramel at bottom-right
    primary3D = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6A4736), // Light specular hit on espresso
            Color(0xFF54382B), // Deep espresso body
            Color(0xFF432A1F), // Shadow depth
            Color(0xFF382319)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    ),

    // 3D Secondary Accent: Warm Caramel to Golden Cinnamon
    secondary3D = Brush.linearGradient(
        colors = listOf(
            Color(0xFFD6AB83), // Top highlight
            Color(0xFFC69A72), // Warm caramel
            Color(0xFFB5875F), // Rich caramel base
            Color(0xFFA2734C)  // Shadow contour
        )
    ),

    // Hero Total Sales 3D Card: Deep Roasted Espresso with golden caramel horizon
    heroCard3D = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4A3125),
            Color(0xFF3B251B),
            Color(0xFF2E1C14)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    ),

    // 3D Convex Card Surface: Subtle top-down specular falloff
    cardSurface3D = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFFCF9F5),
            Color(0xFFFAF5EE)
        )
    ),

    // 3D Froth Container: Soft latte cream with gentle depth
    container3D = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFAF3EB),
            Color(0xFFF5ECE2),
            Color(0xFFEFE4D6)
        )
    ),

    // 3D Beveled Border: Simulates light hitting top-left corner
    beveledBorder3D = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.95f),   // Top-left specular gleam
            Color(0xFFE6D7C8),                 // Mid border
            Color(0xFFD1BAA3).copy(alpha = 0.8f) // Bottom-right shadow edge
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    ),

    // Interactive Action Bevel
    interactiveBevel3D = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFC69A72),
            Color(0xFF54382B)
        )
    ),

    // Ambient 3D Glow
    ambientGlow3D = Brush.radialGradient(
        colors = listOf(
            Color(0xFFC69A72).copy(alpha = 0.35f),
            Color(0xFF54382B).copy(alpha = 0.12f),
            Color.Transparent
        )
    ),

    // Pressed 3D Inset State
    pressed3D = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF382319),
            Color(0xFF482F22),
            Color(0xFF54382B)
        )
    ),

    // Dark Card Surface
    darkCardSurface3D = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF32231A),
            Color(0xFF281C14)
        )
    )
)

val DarkShopFlowGradients = ShopFlowGradients(
    primary3D = Brush.linearGradient(
        colors = listOf(
            Color(0xFFD6AB83),
            Color(0xFFC69A72),
            Color(0xFFB5875F)
        )
    ),
    secondary3D = Brush.linearGradient(
        colors = listOf(
            Color(0xFF7A543E),
            Color(0xFF54382B),
            Color(0xFF3B251B)
        )
    ),
    heroCard3D = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2D1E16),
            Color(0xFF20150F),
            Color(0xFF140D09)
        )
    ),
    cardSurface3D = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF34241B),
            Color(0xFF281C14)
        )
    ),
    container3D = Brush.linearGradient(
        colors = listOf(
            Color(0xFF3D2A1F),
            Color(0xFF332319)
        )
    ),
    beveledBorder3D = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6E4D38),
            Color(0xFF4A3427),
            Color(0xFF332319)
        )
    ),
    interactiveBevel3D = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFD6AB83),
            Color(0xFF7A543E)
        )
    ),
    ambientGlow3D = Brush.radialGradient(
        colors = listOf(
            Color(0xFFC69A72).copy(alpha = 0.40f),
            Color(0xFF54382B).copy(alpha = 0.20f),
            Color.Transparent
        )
    ),
    pressed3D = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1C120C),
            Color(0xFF2B1D15)
        )
    ),
    darkCardSurface3D = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF32231A),
            Color(0xFF281C14)
        )
    )
)

val LocalShopFlowGradients = staticCompositionLocalOf { LightShopFlowGradients }
