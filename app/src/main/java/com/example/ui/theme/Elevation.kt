package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom 3D-inspired elevation specifications for KarobarFlow Material 3 Theme.
 * Supports tactile depth, ambient soft shadows, directional spot shadows, and glow diffusion.
 */
@Immutable
data class ShopFlowElevations(
    val none: Dp = 0.dp,
    val subtle: Dp = 2.dp,
    val card: Dp = 4.dp,
    val interactive: Dp = 8.dp,
    val floating: Dp = 14.dp,
    val hero3D: Dp = 20.dp,
    val ambientShadowColor: Color = Color(0x12362217),
    val spotShadowColor: Color = Color(0x2454382B),
    val glowShadowColor: Color = Color(0x33C69A72)
)

val LightShopFlowElevations = ShopFlowElevations(
    none = 0.dp,
    subtle = 2.dp,
    card = 5.dp,
    interactive = 8.dp,
    floating = 14.dp,
    hero3D = 22.dp,
    ambientShadowColor = Color(0x14362217),
    spotShadowColor = Color(0x2854382B),
    glowShadowColor = Color(0x38C69A72)
)

val DarkShopFlowElevations = ShopFlowElevations(
    none = 0.dp,
    subtle = 2.dp,
    card = 6.dp,
    interactive = 10.dp,
    floating = 16.dp,
    hero3D = 24.dp,
    ambientShadowColor = Color(0x40000000),
    spotShadowColor = Color(0x60000000),
    glowShadowColor = Color(0x40C69A72)
)

val LocalShopFlowElevations = staticCompositionLocalOf { LightShopFlowElevations }

/**
 * Modifier that applies high-fidelity 3D elevation with layered ambient and spot shadows
 * tailored for the coffee-and-caramel futuristic design aesthetic.
 */
fun Modifier.futuristic3DElevation(
    elevation: Dp,
    shape: Shape = RoundedCornerShape(16.dp),
    ambientColor: Color? = null,
    spotColor: Color? = null,
    clip: Boolean = false
): Modifier = this.then(
    Modifier.shadow(
        elevation = elevation,
        shape = shape,
        clip = clip,
        ambientColor = ambientColor ?: Color(0x18362217),
        spotColor = spotColor ?: Color(0x2A54382B)
    )
)

/**
 * Modifier that renders subtle specular 3D light edges (top-left highlight and bottom-right shadow)
 * to impart genuine three-dimensional volume to cards and buttons.
 */
fun Modifier.futuristic3DBevel(
    cornerRadius: Dp = 16.dp,
    highlightColor: Color = Color.White.copy(alpha = 0.65f),
    shadowColor: Color = Color(0x1A362217)
): Modifier = this.then(
    Modifier.drawBehind {
        val strokeWidth = 1.2.dp.toPx()
        // Top & Left Specular Highlight
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(highlightColor, highlightColor.copy(alpha = 0.1f))
            ),
            start = Offset(cornerRadius.toPx(), 0f),
            end = Offset(size.width - cornerRadius.toPx(), 0f),
            strokeWidth = strokeWidth
        )
        // Bottom Shadow Line for 3D Inset/Depth
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(shadowColor.copy(alpha = 0.1f), shadowColor)
            ),
            start = Offset(cornerRadius.toPx(), size.height),
            end = Offset(size.width - cornerRadius.toPx(), size.height),
            strokeWidth = strokeWidth
        )
    }
)
