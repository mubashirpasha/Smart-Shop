package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopSecondary

@Composable
fun SalesTrendChart(
    dataPoints: List<Float>,
    labels: List<String>,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(dataPoints) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        glowColor = ShopPrimary,
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sales Performance Trajectory",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Dynamic Velocity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ShopEmeraldAccent)
                    )
                    Text(
                        text = "LIVE FLOW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ShopEmeraldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
            val outlineColor = MaterialTheme.colorScheme.outline

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                if (dataPoints.isEmpty() || dataPoints.all { it == 0f }) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(
                            color = outlineColor,
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = 2f
                        )
                    }
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val maxVal = (dataPoints.maxOrNull() ?: 1f).coerceAtLeast(10f)
                        val minVal = (dataPoints.minOrNull() ?: 0f).coerceAtLeast(0f)
                        val range = (maxVal - minVal).coerceAtLeast(1f)
                        val progress = animatedProgress.value

                        val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)

                        val points = dataPoints.mapIndexed { index, value ->
                            val x = index * stepX
                            val normalizedY = ((value - minVal) / range) * progress
                            val y = height - (normalizedY * (height * 0.72f) + height * 0.14f)
                            Offset(x, y)
                        }

                        // Horizontal subtle grid guidelines
                        val gridLines = 3
                        for (i in 0..gridLines) {
                            val y = height * (i.toFloat() / gridLines)
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1f
                            )
                        }

                        if (points.isNotEmpty()) {
                            val strokePath = Path()
                            val fillPath = Path()

                            strokePath.moveTo(points.first().x, points.first().y)
                            fillPath.moveTo(points.first().x, height)
                            fillPath.lineTo(points.first().x, points.first().y)

                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                                val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

                                strokePath.cubicTo(
                                    controlPoint1.x, controlPoint1.y,
                                    controlPoint2.x, controlPoint2.y,
                                    p1.x, p1.y
                                )
                                fillPath.cubicTo(
                                    controlPoint1.x, controlPoint1.y,
                                    controlPoint2.x, controlPoint2.y,
                                    p1.x, p1.y
                                )
                            }

                            fillPath.lineTo(points.last().x, height)
                            fillPath.close()

                            // Subtle Area Fill
                            val fillGradient = Brush.verticalGradient(
                                colors = listOf(
                                    ShopPrimary.copy(alpha = 0.22f * progress),
                                    ShopSecondary.copy(alpha = 0.05f * progress),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = height
                            )
                            drawPath(fillPath, brush = fillGradient)

                            // Emerald Trend Stroke
                            val strokeGradient = Brush.horizontalGradient(
                                colors = listOf(ShopPrimary, ShopSecondary)
                            )
                            drawPath(
                                path = strokePath,
                                brush = strokeGradient,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Data Nodes
                            for (p in points) {
                                drawCircle(
                                    color = ShopPrimary.copy(alpha = 0.20f * progress),
                                    radius = 6.dp.toPx(),
                                    center = p
                                )
                                drawCircle(
                                    color = ShopPrimary,
                                    radius = 3.dp.toPx(),
                                    center = p
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 1.5.dp.toPx(),
                                    center = p
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Period Labels
            if (labels.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    labels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
