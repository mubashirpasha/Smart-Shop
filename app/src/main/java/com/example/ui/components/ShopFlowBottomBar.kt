package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import kotlin.math.roundToInt

data class CurvedBottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Curved Animated Floating Bubble Navigation Bar matching the UI/UX states design
 * with an elevated circular bubble, smooth curved crest, and responsive tab states.
 */
@Composable
fun ShopFlowBottomBar(
    currentRoute: String?,
    language: String = "en",
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUrdu = language == "ur"
    val navItems = remember(isUrdu) {
        listOf(
            CurvedBottomNavItem(
                screen = Screen.Dashboard,
                label = if (isUrdu) "ہوم" else "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home
            ),
            CurvedBottomNavItem(
                screen = Screen.Pos,
                label = if (isUrdu) "سیلز" else "POS",
                selectedIcon = Icons.Filled.CreditCard,
                unselectedIcon = Icons.Outlined.CreditCard
            ),
            CurvedBottomNavItem(
                screen = Screen.Inventory,
                label = if (isUrdu) "اسٹاک" else "Inventory",
                selectedIcon = Icons.Filled.Inventory2,
                unselectedIcon = Icons.Outlined.Inventory2
            ),
            CurvedBottomNavItem(
                screen = Screen.Analytics,
                label = if (isUrdu) "تجزیات" else "Analytics",
                selectedIcon = Icons.Filled.Assessment,
                unselectedIcon = Icons.Outlined.Assessment
            ),
            CurvedBottomNavItem(
                screen = Screen.Settings,
                label = if (isUrdu) "سیٹنگز" else "Settings",
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person
            )
        )
    }

    // Determine current active tab index (default to 0 if not matching)
    val selectedIndex = remember(currentRoute) {
        val idx = navItems.indexOfFirst { item ->
            currentRoute == item.screen.route ||
                    (item.screen == Screen.Analytics && currentRoute == Screen.Reports.route) ||
                    (item.screen == Screen.Reports && currentRoute == Screen.Analytics.route)
        }
        if (idx >= 0) idx else 0
    }

    // Smooth spring animation for the curved wave and circular bubble position
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "navBubbleGlider"
    )

    // Palette harmonized with the app's warm artisanal coffee / espresso & caramel theme
    val isDark = MaterialTheme.colorScheme.surface.let {
        (it.red * 0.299 + it.green * 0.587 + it.blue * 0.114) < 0.5
    }

    // Colors matching the ShopFlow coffee brand palette:
    // Espresso (#54382B), Caramel (#C69A72), Warm Cream (#FAF5EE / #F5ECE2)
    val barColor = if (isDark) Color(0xFF281C14) else Color(0xFFFFFFFF)
    val bubbleColor = if (isDark) Color(0xFF36251B) else Color(0xFFFFFFFF)
    val bubbleBorderColor = if (isDark) Color(0xFFC69A72) else Color(0xFF54382B) // App Primary Espresso/Caramel
    val bubbleInnerTint = if (isDark) Color(0xFF4A3427) else Color(0xFFF5ECE2) // Warm latte froth tint
    val activeIconTint = if (isDark) Color(0xFFFDF8F3) else Color(0xFF54382B)
    val inactiveIconTint = if (isDark) Color(0xFF9E8A7B) else Color(0xFF7D6556)
    val activeTextTint = if (isDark) Color(0xFFFDF8F3) else Color(0xFF382319)
    val inactiveTextTint = if (isDark) Color(0xFF9E8A7B) else Color(0xFF7D6556)
    val outlineBorderColor = if (isDark) Color(0xFF4A3427) else Color(0xFFE6D7C8)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .height(92.dp)
            .testTag("shop_flow_curved_bottom_bar")
    ) {
        val totalWidth = maxWidth
        val totalWidthPx = constraints.maxWidth.toFloat()
        val totalHeightPx = constraints.maxHeight.toFloat()
        val itemCount = navItems.size
        val itemWidthPx = totalWidthPx / itemCount

        // Animated target center X in pixels
        val bubbleCenterX = itemWidthPx * (animatedIndex + 0.5f)

        // Wave curve constants (in pixels)
        val barTop = 22.dp
        val barBottom = 90.dp
        val cornerRadius = 24.dp
        val crestHeight = 18.dp
        val crestHalfWidth = 44.dp

        // 1. Canvas Layer: Draws the curved white background bar with smooth wave crest
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barTopPx = barTop.toPx()
            val barBottomPx = barBottom.toPx()
            val cornerRadiusPx = cornerRadius.toPx()
            val crestHeightPx = crestHeight.toPx()
            val crestHalfWidthPx = crestHalfWidth.toPx()

            val leftBase = (bubbleCenterX - crestHalfWidthPx).coerceAtLeast(cornerRadiusPx)
            val rightBase = (bubbleCenterX + crestHalfWidthPx).coerceAtMost(size.width - cornerRadiusPx)

            val path = Path().apply {
                // Top-left corner
                moveTo(0f, barTopPx + cornerRadiusPx)
                quadraticTo(0f, barTopPx, cornerRadiusPx, barTopPx)

                // Flat line to start of wave crest
                lineTo(leftBase, barTopPx)

                // Smooth S-curve up to crest top
                cubicTo(
                    leftBase + (bubbleCenterX - leftBase) * 0.45f, barTopPx,
                    bubbleCenterX - crestHalfWidthPx * 0.42f, barTopPx - crestHeightPx,
                    bubbleCenterX, barTopPx - crestHeightPx
                )

                // Smooth S-curve down from crest top
                cubicTo(
                    bubbleCenterX + crestHalfWidthPx * 0.42f, barTopPx - crestHeightPx,
                    rightBase - (rightBase - bubbleCenterX) * 0.45f, barTopPx,
                    rightBase, barTopPx
                )

                // Flat line to top-right corner
                lineTo(size.width - cornerRadiusPx, barTopPx)
                quadraticTo(size.width, barTopPx, size.width, barTopPx + cornerRadiusPx)

                // Right edge to bottom-right corner
                lineTo(size.width, barBottomPx - cornerRadiusPx)
                quadraticTo(size.width, barBottomPx, size.width - cornerRadiusPx, barBottomPx)

                // Bottom edge to bottom-left corner
                lineTo(cornerRadiusPx, barBottomPx)
                quadraticTo(0f, barBottomPx, 0f, barBottomPx - cornerRadiusPx)

                close()
            }

            // Draw filled bar body
            drawPath(path = path, color = barColor, style = Fill)

            // Draw clean subtle border around the bar
            drawPath(path = path, color = outlineBorderColor.copy(alpha = 0.7f), style = Stroke(width = 1.2.dp.toPx()))
        }

        // 2. The Elevated Floating Circular Bubble (Glides across with animatedIndex)
        val bubbleSize = 54.dp
        val bubbleSizePx = 54.dp.value * (totalWidthPx / totalWidth.value)
        val bubbleXOffset = (bubbleCenterX - (bubbleSizePx / 2f)).roundToInt()

        Box(
            modifier = Modifier
                .offset { IntOffset(x = bubbleXOffset, y = 1.dp.roundToPx()) }
                .size(bubbleSize)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = bubbleBorderColor.copy(alpha = 0.25f),
                    spotColor = bubbleBorderColor.copy(alpha = 0.35f)
                )
                .clip(CircleShape)
                .background(bubbleColor)
                .border(2.5.dp, bubbleBorderColor, CircleShape)
                .testTag("nav_elevated_bubble"),
            contentAlignment = Alignment.Center
        ) {
            // Inner soft lavender circular backing
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bubbleInnerTint),
                contentAlignment = Alignment.Center
            ) {
                val currentItem = navItems.getOrNull(selectedIndex) ?: navItems.first()
                Icon(
                    imageVector = currentItem.selectedIcon,
                    contentDescription = currentItem.label,
                    tint = activeIconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 3. Tab Items Row (Labels and Inactive Icons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onNavigate(item.screen)
                        }
                        .testTag("nav_${item.screen.route}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isSelected) {
                            // When selected, the icon is in the bubble above, so we push label slightly down
                            Spacer(modifier = Modifier.height(28.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    letterSpacing = 0.2.sp
                                ),
                                color = activeTextTint,
                                maxLines = 1
                            )
                        } else {
                            // Unselected item: outlined icon and regular label
                            Icon(
                                imageVector = item.unselectedIcon,
                                contentDescription = item.label,
                                tint = inactiveIconTint,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.5.sp
                                ),
                                color = inactiveTextTint,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 4. Modern Bottom Gesture Home Indicator Pill (as seen in the mockup)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
                .width(76.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isDark) Color(0xFF634635) else Color(0xFFD7C5B5))
        )
    }
}
