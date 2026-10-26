package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ShopFlowTheme
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopSecondary
import com.example.ui.theme.futuristic3DBevel
import com.example.ui.theme.futuristic3DElevation

@Composable
fun FuturisticButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isSecondary: Boolean = false,
    testTag: String? = null,
    enabled: Boolean = true
) {
    val elevations = ShopFlowTheme.elevations
    val gradients = ShopFlowTheme.gradients

    val brush = if (isSecondary) {
        gradients.container3D
    } else {
        gradients.primary3D
    }

    val contentColor = if (isSecondary) {
        ShopPrimary
    } else {
        Color(0xFFFDF8F3)
    }

    val elevationDp = if (isSecondary) elevations.subtle else elevations.interactive

    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .futuristic3DElevation(
                elevation = elevationDp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = elevations.ambientShadowColor,
                spotColor = if (isSecondary) elevations.ambientShadowColor else elevations.spotShadowColor
            )
            .clip(RoundedCornerShape(16.dp))
            .background(brush)
            .border(
                width = 1.dp,
                brush = if (isSecondary) gradients.beveledBorder3D else gradients.interactiveBevel3D,
                shape = RoundedCornerShape(16.dp)
            )
            .futuristic3DBevel(
                cornerRadius = 16.dp,
                highlightColor = if (isSecondary) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.25f),
                shadowColor = if (isSecondary) Color(0x1A54382B) else Color(0x33000000)
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = if (isSecondary) ShopSecondary else Color.White),
                onClick = onClick
            )
            .defaultMinSize(minHeight = 44.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
        }
    }
}
