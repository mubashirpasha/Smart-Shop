package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SaleEntity
import com.example.data.repository.ShopFlowRepository
import com.example.localization.KarobarFormatter
import com.example.localization.getPaymentMethodLabel
import com.example.ui.components.DailyCashClosingDialog
import com.example.ui.components.ExportReportDialog
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.GradientMetricCard
import com.example.ui.components.ProductFormDialog
import com.example.ui.components.QuickRestockDialog
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.SalesTrendChart
import com.example.ui.components.SectionHeader
import com.example.ui.components.TimeFilterChips
import com.example.ui.theme.ShopAmberAccent
import com.example.ui.theme.ShopBorderLight
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopFlowTheme
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopPrimaryVariant
import com.example.ui.theme.ShopRoseAccent
import com.example.ui.theme.ShopSecondary
import com.example.ui.theme.ShopSuccessBackground
import com.example.ui.theme.futuristic3DBevel
import com.example.ui.theme.futuristic3DElevation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    repository: ShopFlowRepository,
    onNavigateToPos: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToReports: () -> Unit = onNavigateToAnalytics,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var showRestockDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showCashClosingDialog by remember { mutableStateOf(false) }
    var selectedSaleForReceipt by remember { mutableStateOf<SaleEntity?>(null) }

    val isUrdu = uiState.settings.language == "ur"
    val currency = uiState.settings.currencySymbol
    val expiringProducts = remember(uiState.allProducts) {
        uiState.allProducts.filter { it.isExpiringSoon || it.isExpired }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ShopPrimary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Compact Header with App/Business Identity and Verified Offline Status
        item {
            DashboardCompactHeader(
                storeName = uiState.settings.storeName,
                isUrdu = isUrdu
            )
        }

        // 2. Today's Sales and Total Sales Cards
        // Total Sales Card (Subtle emerald green visual highlight with white text)
        item {
            TotalSalesHighlightCard(
                totalRevenue = uiState.metrics.totalRevenue,
                totalOrders = uiState.metrics.totalSalesCount,
                currency = currency,
                isUrdu = isUrdu,
                onNewSaleClick = onNavigateToPos
            )
        }

        // Today's Sales Card (Clean white card with subtle shadow and border)
        item {
            TodaySalesMetricCard(
                todayRevenue = uiState.metrics.todaySalesRevenue,
                todayOrders = uiState.metrics.todaySalesCount,
                todayAvgTicket = uiState.metrics.todayAverageOrderValue,
                currency = currency,
                isUrdu = isUrdu
            )
        }

        // 3. Gross Profit and Products in Stock Cards (Responsive 2-column grid)
        item {
            FinancialMetricRow(
                grossProfit = uiState.metrics.grossProfit,
                profitMargin = uiState.metrics.grossProfitMargin,
                totalProducts = uiState.metrics.totalProducts,
                totalStockUnits = uiState.metrics.totalStockUnits,
                currency = currency,
                isUrdu = isUrdu,
                onGrossProfitClick = onNavigateToAnalytics,
                onProductsInStockClick = onNavigateToInventory
            )
        }

        // 4. Clearly Grouped Quick Actions: New Sale, Inventory, Analytics, Add Product, Cash Closing
        item {
            DashboardQuickActions(
                isUrdu = isUrdu,
                onNewSale = onNavigateToPos,
                onNavigateToInventory = onNavigateToInventory,
                onAddProduct = { showAddProductDialog = true },
                onRestock = { showRestockDialog = true },
                onViewAnalytics = onNavigateToAnalytics,
                onCashClosing = { showCashClosingDialog = true }
            )
        }

        // 5. Stock Summary Section
        item {
            StockSummaryCard(
                totalProducts = uiState.metrics.totalProducts,
                totalUnits = uiState.metrics.totalStockUnits,
                costValue = uiState.metrics.inventoryCostValue,
                retailValue = uiState.metrics.inventoryRetailValue,
                currency = currency,
                isUrdu = isUrdu,
                onRestockClick = { showRestockDialog = true },
                onViewInventoryClick = onNavigateToInventory
            )
        }

        // Time Period Filter & Sales Trend Chart
        item {
            TimeFilterChips(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = { viewModel.selectPeriod(it) }
            )
        }

        item {
            SalesTrendChart(
                dataPoints = uiState.metrics.chartPoints,
                labels = uiState.metrics.chartLabels,
                currencySymbol = currency
            )
        }

        // 6. Low-Stock Alerts (if present)
        if (uiState.lowStockAlerts.isNotEmpty()) {
            item {
                SectionHeader(
                    title = if (isUrdu) "کم اسٹاک کی اطلاع" else "Low-Stock Alerts",
                    indicatorColor = ShopRoseAccent,
                    actionText = if (isUrdu) "سب اسٹاک کریں (${uiState.lowStockAlerts.size})" else "Restock All (${uiState.lowStockAlerts.size})",
                    onActionClick = { showRestockDialog = true }
                )
            }

            items(uiState.lowStockAlerts) { product ->
                LowStockAlertCard(
                    productName = product.name,
                    sku = product.sku,
                    stockQuantity = product.stockQuantity,
                    minThreshold = product.minStockThreshold,
                    onRestock = { viewModel.restockProduct(product.id, 10) },
                    isUrdu = isUrdu
                )
            }
        }

        // 7. Product Expiry & Batch Alerts (if present)
        if (expiringProducts.isNotEmpty()) {
            item {
                SectionHeader(
                    title = if (isUrdu) "میعاد ختم ہونے کی اطلاع" else "Expiry Alerts",
                    indicatorColor = ShopAmberAccent,
                    actionText = if (isUrdu) "انوینٹری میں دیکھیں (${expiringProducts.size})" else "View in Vault (${expiringProducts.size})",
                    onActionClick = onNavigateToInventory
                )
            }

            items(expiringProducts) { prod ->
                val isExp = prod.isExpired
                val daysLeft = if (prod.expiryDate != null) {
                    ((prod.expiryDate - System.currentTimeMillis()) / (24 * 3600 * 1000L)).coerceAtLeast(0)
                } else 0
                val alertColor = if (isExp) ShopRoseAccent else ShopAmberAccent
                val alertLabel = if (isExp) "EXPIRED" else "Expiring in ${daysLeft}d"

                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expiry_alert_${prod.id}"),
                    glowColor = alertColor
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prod.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "SKU: ${prod.sku}${if (prod.batchNumber.isNotBlank()) " • Batch: ${prod.batchNumber}" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(alertColor.copy(alpha = 0.18f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = alertLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = alertColor
                            )
                        }
                    }
                }
            }
        }

        // Recent Transactions Section
        item {
            SectionHeader(
                title = if (isUrdu) "حالیہ ٹرانزیکشنز" else "Recent Transactions",
                indicatorColor = ShopPrimary
            )
        }

        if (uiState.recentSales.isEmpty()) {
            item {
                EmptyStateCard(
                    message = if (isUrdu) "ابھی تک کوئی ٹرانزیکشن نہیں ہے۔ پہلا بل بنائیں!" else "No transactions yet. Complete a sale to see it here."
                )
            }
        } else {
            items(uiState.recentSales) { sale ->
                DashboardSaleItemCard(
                    sale = sale,
                    currency = currency,
                    isUrdu = isUrdu,
                    onClick = { selectedSaleForReceipt = sale }
                )
            }
        }
    }

    // Dialogs
    if (showAddProductDialog) {
        ProductFormDialog(
            currencySymbol = currency,
            onDismiss = { showAddProductDialog = false },
            onSaveProduct = { product ->
                viewModel.addProduct(product)
                showAddProductDialog = false
            }
        )
    }

    if (showRestockDialog) {
        QuickRestockDialog(
            products = uiState.allProducts,
            onDismiss = { showRestockDialog = false },
            onRestock = { productId, amount ->
                viewModel.restockProduct(productId, amount)
            }
        )
    }

    if (showExportDialog) {
        ExportReportDialog(
            storeName = uiState.settings.storeName,
            currencySymbol = currency,
            metrics = uiState.metrics,
            onDismiss = { showExportDialog = false }
        )
    }

    selectedSaleForReceipt?.let { sale ->
        ReceiptDialog(
            sale = sale,
            currencySymbol = currency,
            repository = repository,
            onDismiss = { selectedSaleForReceipt = null }
        )
    }

    if (showCashClosingDialog) {
        DailyCashClosingDialog(
            sales = uiState.allSales,
            expenses = uiState.allExpenses,
            storeName = uiState.settings.storeName,
            currencySymbol = currency,
            isUrdu = isUrdu,
            onDismiss = { showCashClosingDialog = false }
        )
    }
}

// 1. Compact Header with App/Business Identity and Verified Offline Status
@Composable
private fun DashboardCompactHeader(
    storeName: String,
    isUrdu: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_compact_header")
    ) {
        // Greeting
        Text(
            text = if (isUrdu) "خوش آمدید!" else "Welcome Back!",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Merchant Selected Store Name
        Text(
            text = storeName,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// 2a. Total Sales Card (Single Visual Highlight: Subtle Emerald Green)
@Composable
private fun TotalSalesHighlightCard(
    totalRevenue: Double,
    totalOrders: Int,
    currency: String,
    isUrdu: Boolean = false,
    onNewSaleClick: () -> Unit
) {
    val hero3DBrush = ShopFlowTheme.gradients.heroCard3D
    val elevations = ShopFlowTheme.elevations

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .futuristic3DElevation(
                elevation = elevations.hero3D,
                shape = RoundedCornerShape(18.dp),
                ambientColor = elevations.ambientShadowColor,
                spotColor = elevations.spotShadowColor
            )
            .clip(RoundedCornerShape(18.dp))
            .background(hero3DBrush)
            .border(
                width = 1.dp,
                brush = ShopFlowTheme.gradients.beveledBorder3D,
                shape = RoundedCornerShape(18.dp)
            )
            .futuristic3DBevel(
                cornerRadius = 18.dp,
                highlightColor = Color.White.copy(alpha = 0.35f),
                shadowColor = Color(0x33000000)
            )
            .padding(20.dp)
            .testTag("hero_total_sales_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUrdu) "مجموعی فروخت (لائف ٹائم)" else "Total Sales",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White.copy(alpha = 0.90f)
                )

                // Clean New Sale Action
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable(onClick = onNewSaleClick)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("hero_btn_new_sale"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCartCheckout,
                            contentDescription = null,
                            tint = ShopPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUrdu) "نیا بل" else "New Sale",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ShopPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = KarobarFormatter.formatCurrency(totalRevenue, currency),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isUrdu) "کل آرڈرز" else "Total Orders",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "$totalOrders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Text(
                        text = if (isUrdu) "اوسط آرڈر ویلیو" else "Average Order Value",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    val avg = if (totalOrders > 0) totalRevenue / totalOrders else 0.0
                    Text(
                        text = KarobarFormatter.formatCurrency(avg, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// 2b. Today's Sales Card (Predominantly White Card)
@Composable
private fun TodaySalesMetricCard(
    todayRevenue: Double,
    todayOrders: Int,
    todayAvgTicket: Double,
    currency: String,
    isUrdu: Boolean = false
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "card_todays_sales"
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ShopSecondary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = ShopPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isUrdu) "آج کی فروخت" else "Today's Sales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (todayOrders > 0) {
                                if (isUrdu) "$todayOrders آرڈرز" else "$todayOrders orders"
                            } else {
                                if (isUrdu) "کوئی آرڈر نہیں" else "No orders yet"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Today's Sales Figure
                Text(
                    text = KarobarFormatter.formatCurrency(todayRevenue, currency),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = ShopPrimary,
                    maxLines = 1,
                    textAlign = TextAlign.End
                )
            }

            if (todayOrders > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isUrdu) "آج کی اوسط بل رقم:" else "Today's Average Order Value:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = KarobarFormatter.formatCurrency(todayAvgTicket, currency),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// 3. Gross Profit and Products in Stock Cards (Responsive 2-column Grid)
@Composable
private fun FinancialMetricRow(
    grossProfit: Double,
    profitMargin: Double,
    totalProducts: Int,
    totalStockUnits: Int,
    currency: String,
    isUrdu: Boolean = false,
    onGrossProfitClick: (() -> Unit)? = null,
    onProductsInStockClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Gross Profit Card (White card, verified margin percentage)
        GradientMetricCard(
            title = if (isUrdu) "مجموعی منافع" else "Gross Profit",
            value = KarobarFormatter.formatCurrency(grossProfit, currency),
            subtitle = "${String.format(Locale.US, "%.1f", profitMargin)}% " + if (isUrdu) "مارجن کی شرح" else "Margin Rate",
            trendText = null,
            isPositiveTrend = true,
            icon = Icons.Default.AttachMoney,
            accentColor = ShopPrimary,
            modifier = Modifier.weight(1f),
            testTag = "card_gross_profit",
            onClick = onGrossProfitClick
        )

        // Products in Stock Card
        GradientMetricCard(
            title = if (isUrdu) "دستیاب پروڈکٹس" else "Products in Stock",
            value = "$totalProducts " + if (isUrdu) "پروڈکٹس" else "Products",
            subtitle = "$totalStockUnits " + if (isUrdu) "کل آئٹمز" else "Units in Stock",
            trendText = null,
            isPositiveTrend = true,
            icon = Icons.Default.Inventory2,
            accentColor = ShopSecondary,
            modifier = Modifier.weight(1f),
            testTag = "card_products_in_stock",
            onClick = onProductsInStockClick
        )
    }
}

// 4. Clearly Grouped Quick Actions: New Sale, Inventory, Analytics, Add Product, Cash Closing
@Composable
private fun DashboardQuickActions(
    isUrdu: Boolean = false,
    onNewSale: () -> Unit,
    onNavigateToInventory: () -> Unit = {},
    onAddProduct: () -> Unit,
    onRestock: () -> Unit = {},
    onViewAnalytics: () -> Unit,
    onCashClosing: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isUrdu) "فوری اقدامات" else "Quick Actions",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                text = if (isUrdu) "نیا بل" else "New Sale",
                icon = Icons.Default.PointOfSale,
                accentColor = ShopPrimary,
                modifier = Modifier.weight(1f),
                onClick = onNewSale,
                testTag = "btn_quick_sale_action"
            )

            QuickActionButton(
                text = if (isUrdu) "انوینٹری" else "Inventory",
                icon = Icons.Default.Inventory2,
                accentColor = ShopSecondary,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToInventory,
                testTag = "btn_quick_inventory_action"
            )

            QuickActionButton(
                text = if (isUrdu) "تجزیات" else "Analytics",
                icon = Icons.Default.Assessment,
                accentColor = ShopPrimary,
                modifier = Modifier.weight(1f),
                onClick = onViewAnalytics,
                testTag = "btn_quick_analytics_action"
            )

            QuickActionButton(
                text = if (isUrdu) "پروڈکٹ" else "Product",
                icon = Icons.Default.Add,
                accentColor = ShopSecondary,
                modifier = Modifier.weight(1f),
                onClick = onAddProduct,
                testTag = "btn_quick_add_product_action"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                text = if (isUrdu) "کیش کلوزنگ (Z-Report)" else "Cash Closing (Z-Report)",
                icon = Icons.Default.PointOfSale,
                accentColor = ShopEmeraldAccent,
                modifier = Modifier.weight(1f),
                onClick = onCashClosing,
                testTag = "btn_quick_cash_closing"
            )

            QuickActionButton(
                text = if (isUrdu) "اسٹاک ری فل" else "Restock Items",
                icon = Icons.Default.Inventory2,
                accentColor = ShopAmberAccent,
                modifier = Modifier.weight(1f),
                onClick = onRestock,
                testTag = "btn_quick_restock_action"
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    text: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// 5. Stock Summary Section
@Composable
private fun StockSummaryCard(
    totalProducts: Int,
    totalUnits: Int,
    costValue: Double,
    retailValue: Double,
    currency: String,
    isUrdu: Boolean = false,
    onRestockClick: () -> Unit,
    onViewInventoryClick: (() -> Unit)? = null
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "card_stock_summary"
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (onViewInventoryClick != null) Modifier.clickable(onClick = onViewInventoryClick)
                            else Modifier
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ShopSecondary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = ShopPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isUrdu) "اسٹاک کا خلاصہ" else "Stock Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isUrdu) "$totalProducts پروڈکٹس • $totalUnits کل آئٹمز" else "$totalProducts Products • $totalUnits Units in Stock",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onViewInventoryClick != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable(onClick = onViewInventoryClick)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("btn_stock_summary_view_inventory")
                        ) {
                            Text(
                                text = if (isUrdu) "دیکھیں" else "View All",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ShopSuccessBackground)
                            .border(1.dp, ShopPrimary.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .clickable(onClick = onRestockClick)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("btn_stock_summary_restock")
                    ) {
                        Text(
                            text = if (isUrdu) "+ اسٹاک" else "+ Restock",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ShopPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isUrdu) "خریداری کی قیمت" else "Stock Purchase Value",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = KarobarFormatter.formatCurrency(costValue, currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isUrdu) "فروخت کی قیمت" else "Stock Sale Value",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = KarobarFormatter.formatCurrency(retailValue, currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ShopPrimary
                    )
                }
            }
        }
    }
}

// 6. Low-Stock Alert Card
@Composable
private fun LowStockAlertCard(
    productName: String,
    sku: String,
    stockQuantity: Int,
    minThreshold: Int,
    onRestock: () -> Unit,
    isUrdu: Boolean = false
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "card_low_stock_alert"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ShopRoseAccent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Alert",
                        tint = ShopRoseAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                ) {
                    Text(
                        text = productName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "SKU: $sku • " + if (isUrdu) "باقی: $stockQuantity (کم از کم: $minThreshold)" else "Only $stockQuantity left (Min: $minThreshold)",
                        style = MaterialTheme.typography.bodySmall,
                        color = ShopRoseAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ShopRoseAccent.copy(alpha = 0.12f))
                    .clickable(onClick = onRestock)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isUrdu) "+10 اضافہ کریں" else "+10 Units",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ShopRoseAccent
                )
            }
        }
    }
}

// 7. Recent Sale Card for Dashboard
@Composable
private fun DashboardSaleItemCard(
    sale: SaleEntity,
    currency: String,
    isUrdu: Boolean = false,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(sale.timestamp) { dateFormat.format(Date(sale.timestamp)) }

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("recent_sale_${sale.invoiceNumber}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ShopSuccessBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = ShopPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = sale.customerName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${sale.invoiceNumber} • $formattedTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = KarobarFormatter.formatCurrency(sale.totalAmount, currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = ShopPrimary
                )
                Text(
                    text = getPaymentMethodLabel(sale.paymentMethod, isUrdu),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "dashboard_empty_state"
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
