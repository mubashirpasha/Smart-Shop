package com.example.ui.screens.hub

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localization.KarobarFormatter
import com.example.ui.components.GlassmorphicCard
import com.example.ui.navigation.Screen
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.theme.ShopAmberAccent
import com.example.ui.theme.ShopBorderDark
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopRoseAccent
import com.example.ui.theme.ShopSecondary

@Composable
fun BusinessHubScreen(
    viewModel: DashboardViewModel,
    onNavigate: (Screen) -> Unit,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    BackHandler { onNavigateBack() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isUrdu = uiState.settings.language == "ur"
    val currency = uiState.settings.currencySymbol
    val metrics = uiState.metrics

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .testTag("business_hub_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header - clean, single title, no tagline
        item {
            Text(
                text = if (isUrdu) "کھاتہ مرکز" else "Business Hub",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // 1. Customers Module Card
        item {
            HubModuleCard(
                title = if (isUrdu) "کسٹمرز و ادھار کھاتہ" else "Customers & Receivables",
                subtitle = if (isUrdu) {
                    "${uiState.allCustomers.size} کسٹمرز • واجب الادا ادھار: ${KarobarFormatter.formatCurrency(metrics.totalReceivables, currency)}"
                } else {
                    "${uiState.allCustomers.size} Customers • Balance Due: ${KarobarFormatter.formatCurrency(metrics.totalReceivables, currency)}"
                },
                icon = Icons.Default.People,
                accentColor = if (metrics.totalReceivables > 0) ShopAmberAccent else ShopPrimary,
                onClick = { onNavigate(Screen.Customers) },
                testTag = "hub_card_customers"
            )
        }

        // 2. Suppliers Module Card
        item {
            HubModuleCard(
                title = if (isUrdu) "سپلائرز و وینڈرز" else "Suppliers & Payables",
                subtitle = if (isUrdu) {
                    "${uiState.allSuppliers.size} سپلائرز • بقایا رقم: ${KarobarFormatter.formatCurrency(metrics.totalPayables, currency)}"
                } else {
                    "${uiState.allSuppliers.size} Vendors • Outstanding: ${KarobarFormatter.formatCurrency(metrics.totalPayables, currency)}"
                },
                icon = Icons.Default.Business,
                accentColor = if (metrics.totalPayables > 0) ShopRoseAccent else ShopPrimary,
                onClick = { onNavigate(Screen.Suppliers) },
                testTag = "hub_card_suppliers"
            )
        }

        // 3. Expenses Module Card
        item {
            HubModuleCard(
                title = if (isUrdu) "کاروباری اخراجات" else "Business Expenses",
                subtitle = if (isUrdu) {
                    "${uiState.allExpenses.size} اندراج شدہ • کل خرچہ: ${KarobarFormatter.formatCurrency(metrics.totalExpenses, currency)}"
                } else {
                    "${uiState.allExpenses.size} Logged • Total: ${KarobarFormatter.formatCurrency(metrics.totalExpenses, currency)}"
                },
                icon = Icons.Default.MoneyOff,
                accentColor = ShopRoseAccent,
                onClick = { onNavigate(Screen.Expenses) },
                testTag = "hub_card_expenses"
            )
        }

        // 4. Financial Reports & P&L
        item {
            HubModuleCard(
                title = if (isUrdu) "مالی رپورٹس اور نفع نقصان" else "Financial Reports & P&L",
                subtitle = if (isUrdu) {
                    "خالص نفع: ${KarobarFormatter.formatCurrency(metrics.netProfit, currency)} • مالیت اسٹاک"
                } else {
                    "Net Profit: ${KarobarFormatter.formatCurrency(metrics.netProfit, currency)} • Stock Valuation"
                },
                icon = Icons.Default.Assessment,
                accentColor = ShopEmeraldAccent,
                onClick = { onNavigate(Screen.Reports) },
                testTag = "hub_card_reports"
            )
        }

        // 5. Invoices & Sales History
        item {
            HubModuleCard(
                title = if (isUrdu) "انوائس ہسٹری و رسیدیں" else "Invoice History",
                subtitle = if (isUrdu) {
                    "${metrics.totalSalesCount} بل ریکارڈ • پی ڈی ایف رسیدیں"
                } else {
                    "${metrics.totalSalesCount} Transactions • PDF Receipts & Search"
                },
                icon = Icons.Default.ReceiptLong,
                accentColor = ShopSecondary,
                onClick = { onNavigate(Screen.Sales) },
                testTag = "hub_card_sales"
            )
        }
    }
}

@Composable
private fun HubModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
