package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ShopFlowTheme
import com.example.ui.theme.futuristic3DBevel
import com.example.ui.theme.futuristic3DElevation

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    borderColor: Color? = null,
    glowColor: Color = MaterialTheme.colorScheme.outline,
    elevation: Dp? = null,
    testTag: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val elevations = ShopFlowTheme.elevations
    val gradients = ShopFlowTheme.gradients
    val effectiveElevation = elevation ?: elevations.card

    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .futuristic3DElevation(
                elevation = effectiveElevation,
                shape = shape,
                ambientColor = elevations.ambientShadowColor,
                spotColor = elevations.spotShadowColor
            )
            .clip(shape)
            .background(gradients.cardSurface3D)
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, shape)
                } else {
                    Modifier.border(1.dp, gradients.beveledBorder3D, shape)
                }
            )
            .futuristic3DBevel(
                cornerRadius = 18.dp,
                highlightColor = Color.White.copy(alpha = 0.7f),
                shadowColor = elevations.ambientShadowColor
            )
            .padding(16.dp),
        content = content
    )
}
