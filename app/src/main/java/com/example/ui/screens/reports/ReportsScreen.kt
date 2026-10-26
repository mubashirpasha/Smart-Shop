package com.example.ui.screens.reports

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.ShopFlowRepository
import com.example.localization.KarobarFormatter
import com.example.localization.KarobarMath
import com.example.ui.components.FuturisticButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.theme.ShopAmberAccent
import com.example.ui.theme.ShopBorderDark
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopRoseAccent
import com.example.ui.theme.ShopSecondary
import java.util.Calendar
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit = onNavigateBack,
    onNavigateToInventory: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currency = uiState.settings.currencySymbol
    val isUrdu = uiState.settings.language == "ur"
    val metrics = uiState.metrics

    val currentTime = System.currentTimeMillis()
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfToday = calendar.timeInMillis
    val startOfWeek = currentTime - (7 * 86400_000L)
    val startOfMonth = currentTime - (30 * 86400_000L)

    val dailySales = metrics.todaySalesRevenue
    val weeklySales = remember(uiState.allSales) {
        uiState.allSales.filter { it.timestamp >= startOfWeek }.sumOf { it.totalAmount }
    }
    val monthlySales = remember(uiState.allSales) {
        uiState.allSales.filter { it.timestamp >= startOfMonth }.sumOf { it.totalAmount }
    }

    val netProfit = metrics.netProfit
    val isNetProfitable = netProfit >= 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .testTag("reports_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Share Report Button - clean single title, no tagline
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUrdu) "کاروباری رپورٹس" else "Reports",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                FuturisticButton(
                    text = if (isUrdu) "شیئر رپورٹ" else "Share P&L",
                    icon = Icons.Default.Share,
                    isSecondary = true,
                    onClick = {
                        val reportText = buildString {
                            appendLine("==========================================")
                            appendLine("  ${uiState.settings.storeName.uppercase(Locale.US)} EXECUTIVE REPORT")
                            appendLine("==========================================")
                            appendLine("SALES REVENUE:")
                            appendLine("  Daily Sales   : ${KarobarFormatter.formatCurrency(dailySales, currency)}")
                            appendLine("  Weekly Sales  : ${KarobarFormatter.formatCurrency(weeklySales, currency)}")
                            appendLine("  Monthly Sales : ${KarobarFormatter.formatCurrency(monthlySales, currency)}")
                            appendLine("  Total Gross   : ${KarobarFormatter.formatCurrency(metrics.totalRevenue, currency)}")
                            appendLine("------------------------------------------")
                            appendLine("PROFITABILITY:")
                            appendLine("  Gross Profit  : ${KarobarFormatter.formatCurrency(metrics.grossProfit, currency)} (${String.format(Locale.US, "%.1f", metrics.grossProfitMargin)}%)")
                            appendLine("  Total Expenses: ${KarobarFormatter.formatCurrency(metrics.totalExpenses, currency)}")
                            appendLine("  NET PROFIT    : ${KarobarFormatter.formatCurrency(netProfit, currency)}")
                            appendLine("------------------------------------------")
                            appendLine("ASSETS & VALUATIONS:")
                            appendLine("  Stock Cost    : ${KarobarFormatter.formatCurrency(metrics.inventoryCostValue, currency)}")
                            appendLine("  Retail Value  : ${KarobarFormatter.formatCurrency(metrics.inventoryRetailValue, currency)}")
                            appendLine("  Receivables   : ${KarobarFormatter.formatCurrency(metrics.totalReceivables, currency)}")
                            appendLine("  Payables      : ${KarobarFormatter.formatCurrency(metrics.totalPayables, currency)}")
                            appendLine("==========================================")
                        }
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Executive Report"))
                    },
                    testTag = "btn_share_report"
                )
            }
        }

        // Net Profit Hero Card
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = if (isNetProfitable) ShopEmeraldAccent else ShopRoseAccent
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Net Business Profit (After Expenses)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = KarobarFormatter.formatCurrency(netProfit, currency),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isNetProfitable) ShopEmeraldAccent else ShopRoseAccent
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background((if (isNetProfitable) ShopEmeraldAccent else ShopRoseAccent).copy(alpha = 0.2f))
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isNetProfitable) Icons.Default.TrendingUp else Icons.Default.MoneyOff,
                                contentDescription = null,
                                tint = if (isNetProfitable) ShopEmeraldAccent else ShopRoseAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Gross Profit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = KarobarFormatter.formatCurrency(metrics.grossProfit, currency), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ShopCyanAccent)
                        }

                        Column {
                            Text(text = "Gross Margin", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${String.format(Locale.US, "%.1f", metrics.grossProfitMargin)}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ShopEmeraldAccent)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Total Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = KarobarFormatter.formatCurrency(metrics.totalExpenses, currency), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ShopRoseAccent)
                        }
                    }
                }
            }
        }

        // Sales Horizon Breakdown: Daily, Weekly, Monthly
        item {
            Text(
                text = "Sales Horizon Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Daily
                GlassmorphicCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isUrdu) "آج کی سیلز" else "Daily Sales",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = KarobarFormatter.formatCurrency(dailySales, currency),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Weekly
                GlassmorphicCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isUrdu) "7 دن کی سیلز" else "7-Day Sales",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = KarobarFormatter.formatCurrency(weeklySales, currency),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Monthly
                GlassmorphicCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isUrdu) "30 دن کی سیلز" else "30-Day Sales",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = KarobarFormatter.formatCurrency(monthlySales, currency),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Inventory Stock Valuation
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUrdu) "انوینٹری اسٹاک کی مالیت" else "Inventory Stock Valuation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (isUrdu) "انوینٹری دیکھیں →" else "View Inventory →",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ShopPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onNavigateToInventory)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Vault Units in Stock", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${metrics.totalStockUnits} units", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Asset Valuation (Cost Basis)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = KarobarFormatter.formatCurrency(metrics.inventoryCostValue, currency), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ShopAmberAccent)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Retail Valuation (Potential Sales)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = KarobarFormatter.formatCurrency(metrics.inventoryRetailValue, currency), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ShopEmeraldAccent)
                    }

                    val unrealizedProfit = KarobarMath.subtract(metrics.inventoryRetailValue, metrics.inventoryCostValue)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Unrealized Inventory Profit", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = KarobarFormatter.formatCurrency(unrealizedProfit, currency), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ShopCyanAccent)
                    }
                }
            }
        }

        // Best-Selling Products Ranking
        item {
            Text(
                text = "Best-Selling Products",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (metrics.bestSellers.isEmpty()) {
            item {
                Text(text = "No sales recorded yet to compute rankings.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(metrics.bestSellers) { item ->
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(ShopPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = ShopPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.productName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${item.unitsSold} units sold • In stock: ${item.remainingStock}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Text(
                            text = KarobarFormatter.formatCurrency(item.totalRevenue, currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ShopEmeraldAccent,
                            maxLines = 1,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
