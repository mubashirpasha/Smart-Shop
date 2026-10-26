package com.example.ui.screens.sales

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import com.example.localization.KarobarFormatter
import com.example.localization.KarobarPaymentMethods
import com.example.localization.getPaymentMethodLabel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SaleEntity
import com.example.data.repository.ShopFlowRepository
import com.example.ui.components.FuturisticButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.QuickSaleDialog
import com.example.ui.components.ReceiptDialog
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesHistoryScreen(
    viewModel: DashboardViewModel,
    repository: ShopFlowRepository,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    BackHandler { onNavigateBack() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSaleDialog by remember { mutableStateOf(false) }
    var selectedSaleForReceipt by remember { mutableStateOf<SaleEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPaymentFilter by remember { mutableStateOf("ALL") }

    val currency = uiState.settings.currencySymbol
    val isUrdu = uiState.settings.language == "ur"
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.US) }

    val filteredSales = uiState.recentSales.filter { sale ->
        val matchesSearch = sale.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                sale.customerNote.contains(searchQuery, ignoreCase = true)
        val matchesPayment = selectedPaymentFilter == "ALL" || sale.paymentMethod == selectedPaymentFilter
        matchesSearch && matchesPayment
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .testTag("sales_history_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title Bar & New Sale Action - clean single title, no tagline
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUrdu) "انوائس ہسٹری" else "Invoice History",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                FuturisticButton(
                    text = if (isUrdu) "+ نیا بل" else "+ New Sale",
                    icon = Icons.Default.PointOfSale,
                    onClick = { showSaleDialog = true },
                    testTag = "sales_btn_new"
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                placeholder = { Text("Search by invoice # or note...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sales_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShopPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Payment Channel Filter Chips (Pakistani Payment Methods)
        item {
            val isUrdu = uiState.settings.language == "ur"
            val filterOptions = listOf(
                "ALL" to (if (isUrdu) "تمام" else "All"),
                "CASH" to (if (isUrdu) "نقد کیش" else "Cash"),
                "BANK" to (if (isUrdu) "بینک" else "Bank Transfer"),
                "JAZZCASH" to (if (isUrdu) "جاز کیش" else "JazzCash"),
                "EASYPAISA" to (if (isUrdu) "ایزی پیسہ" else "Easypaisa"),
                "CREDIT" to (if (isUrdu) "ادھار" else "Credit / Udhaar"),
                "OTHER" to (if (isUrdu) "دیگر" else "Other")
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { (code, label) ->
                    val isSelected = selectedPaymentFilter == code
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) ShopEmeraldAccent else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedPaymentFilter = code }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (filteredSales.isEmpty()) {
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching transactions found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Record transactions from inventory with 1 tap",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredSales) { sale ->
                val dateString = remember(sale.timestamp) { dateFormat.format(Date(sale.timestamp)) }
                val isUrdu = uiState.settings.language == "ur"
                val paymentIcon = when (sale.paymentMethod) {
                    "CASH" -> Icons.Default.Payments
                    "BANK" -> Icons.Default.AccountBalance
                    "JAZZCASH", "EASYPAISA" -> Icons.Default.PhoneAndroid
                    "CREDIT" -> Icons.Default.AccountBalanceWallet
                    "CARD" -> Icons.Default.CreditCard
                    "DIGITAL" -> Icons.Default.QrCode2
                    else -> Icons.Default.MoreHoriz
                }
                val methodLabel = getPaymentMethodLabel(sale.paymentMethod, isUrdu)

                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedSaleForReceipt = sale },
                    elevation = 3.dp
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
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = paymentIcon,
                                    contentDescription = methodLabel,
                                    tint = ShopCyanAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = sale.invoiceNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = dateString,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${sale.itemCount} items • $methodLabel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ShopSecondary
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${KarobarFormatter.formatCurrency(sale.totalAmount, currency)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ShopEmeraldAccent
                            )
                            Text(
                                text = if (isUrdu) "رسید دیکھیں" else "View receipt",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = ShopPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSaleDialog) {
        QuickSaleDialog(
            products = uiState.allProducts,
            currencySymbol = currency,
            onDismiss = { showSaleDialog = false },
            onRecordSale = { sale, items ->
                viewModel.recordSale(sale, items)
            }
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
}
