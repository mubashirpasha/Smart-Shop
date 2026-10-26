package com.example.ui.screens.inventory

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import com.example.ui.components.CameraBarcodeScannerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ProductEntity
import com.example.localization.KarobarFormatter
import com.example.ui.components.FuturisticButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.ProductFormDialog
import com.example.ui.components.ProductVisualBadge
import com.example.ui.components.StockAdjustmentDialog
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.theme.ShopAmberAccent
import com.example.ui.theme.ShopBorderDark
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopRoseAccent
import com.example.ui.theme.ShopSecondary
import java.util.Locale

enum class InventorySortOption(val label: String) {
    NAME_ASC("Name (A-Z)"),
    STOCK_LOW_TO_HIGH("Stock (Low to High)"),
    STOCK_HIGH_TO_LOW("Stock (High to Low)"),
    PRICE_HIGH_TO_LOW("Selling Price (High to Low)"),
    PRICE_LOW_TO_HIGH("Selling Price (Low to High)")
}

@Composable
fun InventoryScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit = onNavigateBack,
    onNavigateToAnalytics: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    BackHandler { onNavigateBack() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var statusFilter by remember { mutableStateOf("ALL") } // "ALL", "LOW_STOCK", "EXPIRING", "EXPIRED"
    var showOnlyLowStock by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(InventorySortOption.STOCK_LOW_TO_HIGH) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    // Dialog controllers
    var isAddingNewProduct by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var productToAdjust by remember { mutableStateOf<ProductEntity?>(null) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    val currency = uiState.settings.currencySymbol
    val isUrdu = uiState.settings.language == "ur"

    val allCategories = remember(uiState.allProducts) {
        listOf("All") + uiState.allProducts.map { it.category }.distinct()
    }

    val lowStockCount = remember(uiState.allProducts) {
        uiState.allProducts.count { it.isLowStock }
    }
    val expiringCount = remember(uiState.allProducts) {
        uiState.allProducts.count { it.isExpiringSoon }
    }
    val expiredCount = remember(uiState.allProducts) {
        uiState.allProducts.count { it.isExpired }
    }

    val processedProducts = remember(
        uiState.allProducts,
        searchQuery,
        selectedCategory,
        statusFilter,
        showOnlyLowStock,
        sortOption
    ) {
        uiState.allProducts
            .filter { product ->
                val matchesQuery = searchQuery.isBlank() ||
                        product.name.contains(searchQuery, ignoreCase = true) ||
                        product.sku.contains(searchQuery, ignoreCase = true) ||
                        product.barcode.contains(searchQuery, ignoreCase = true) ||
                        product.batchNumber.contains(searchQuery, ignoreCase = true) ||
                        product.description.contains(searchQuery, ignoreCase = true)

                val matchesCategory = selectedCategory == "All" || product.category.equals(selectedCategory, ignoreCase = true)
                val matchesStatus = when (statusFilter) {
                    "LOW_STOCK" -> product.isLowStock
                    "EXPIRING" -> product.isExpiringSoon
                    "EXPIRED" -> product.isExpired
                    else -> !showOnlyLowStock || product.isLowStock
                }

                matchesQuery && matchesCategory && matchesStatus
            }
            .sortedWith { a, b ->
                when (sortOption) {
                    InventorySortOption.NAME_ASC -> a.name.compareTo(b.name, ignoreCase = true)
                    InventorySortOption.STOCK_LOW_TO_HIGH -> a.stockQuantity.compareTo(b.stockQuantity)
                    InventorySortOption.STOCK_HIGH_TO_LOW -> b.stockQuantity.compareTo(a.stockQuantity)
                    InventorySortOption.PRICE_HIGH_TO_LOW -> b.sellingPrice.compareTo(a.sellingPrice)
                    InventorySortOption.PRICE_LOW_TO_HIGH -> a.sellingPrice.compareTo(b.sellingPrice)
                }
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .testTag("inventory_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title & Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUrdu) "اسٹاک" else "Inventory",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                FuturisticButton(
                    text = if (isUrdu) "نیا آئٹم" else "Add Item",
                    icon = Icons.Default.Add,
                    onClick = { isAddingNewProduct = true },
                    testTag = "inventory_btn_add"
                )
            }
        }

        // Low-Stock Notifications Banner
        if (lowStockCount > 0) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ShopRoseAccent.copy(alpha = 0.12f))
                        .border(1.2.dp, ShopRoseAccent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .clickable { showOnlyLowStock = !showOnlyLowStock }
                        .padding(14.dp)
                        .testTag("low_stock_notification_banner")
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ShopRoseAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = "Alert",
                                    tint = ShopRoseAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "$lowStockCount Items Depleted Below Minimum Level",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ShopRoseAccent
                                )
                                Text(
                                    text = if (showOnlyLowStock) "Showing only low-stock items (tap to reset filter)" else "Tap to isolate and review critical low stock items",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ShopRoseAccent.copy(alpha = 0.25f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (showOnlyLowStock) "Show All" else "Filter Low",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ShopRoseAccent
                            )
                        }
                    }
                }
            }
        }

        // Search Bar with Barcode / Clear Actions
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
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = { showBarcodeScanner = true },
                            modifier = Modifier.testTag("btn_inventory_camera_scan")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Camera Barcode Scanner",
                                tint = ShopCyanAccent
                            )
                        }
                    }
                },
                placeholder = { Text("Search by name, SKU, barcode, description...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inventory_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShopPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Status Filter Chips: All, Low Stock, Expiring Soon, Expired
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val isSelected = statusFilter == "ALL"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ShopPrimary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { statusFilter = "ALL"; showOnlyLowStock = false }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "All Items",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (lowStockCount > 0) {
                    item {
                        val isSelected = statusFilter == "LOW_STOCK"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ShopRoseAccent else ShopRoseAccent.copy(alpha = 0.15f))
                                .clickable { statusFilter = if (isSelected) "ALL" else "LOW_STOCK" }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Low Stock ($lowStockCount)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else ShopRoseAccent
                            )
                        }
                    }
                }

                if (expiringCount > 0) {
                    item {
                        val isSelected = statusFilter == "EXPIRING"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ShopAmberAccent else ShopAmberAccent.copy(alpha = 0.15f))
                                .clickable { statusFilter = if (isSelected) "ALL" else "EXPIRING" }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Expiring Soon ($expiringCount)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else ShopAmberAccent
                            )
                        }
                    }
                }

                if (expiredCount > 0) {
                    item {
                        val isSelected = statusFilter == "EXPIRED"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ShopRoseAccent else ShopRoseAccent.copy(alpha = 0.2f))
                                .clickable { statusFilter = if (isSelected) "ALL" else "EXPIRED" }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Expired ($expiredCount)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else ShopRoseAccent
                            )
                        }
                    }
                }
            }
        }

        // Category Filters & Sort Controls
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Sort Dropdown Button
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .clickable { sortMenuExpanded = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = null,
                                tint = ShopCyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sortOption.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            InventorySortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        sortOption = option
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Horizontal Category Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allCategories) { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) ShopPrimary else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Product Items List
        if (processedProducts.isEmpty()) {
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching items in vault",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search query, clearing filters, or adding a new product.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(processedProducts, key = { it.id }) { product ->
                InventoryProductCard(
                    product = product,
                    currency = currency,
                    onEditClick = { productToEdit = product },
                    onAdjustStockClick = { productToAdjust = product },
                    onDeleteClick = { productToDelete = product }
                )
            }
        }
    }

    // Add Product Dialog
    if (isAddingNewProduct) {
        ProductFormDialog(
            initialProduct = null,
            existingCategories = allCategories,
            currencySymbol = currency,
            onDismiss = { isAddingNewProduct = false },
            onSaveProduct = { newProduct ->
                viewModel.addProduct(newProduct)
            }
        )
    }

    // Edit Product Dialog
    productToEdit?.let { product ->
        ProductFormDialog(
            initialProduct = product,
            existingCategories = allCategories,
            currencySymbol = currency,
            onDismiss = { productToEdit = null },
            onSaveProduct = { updatedProduct ->
                viewModel.updateProduct(updatedProduct)
            }
        )
    }

    // Stock Adjustment Dialog
    productToAdjust?.let { product ->
        StockAdjustmentDialog(
            product = product,
            onDismiss = { productToAdjust = null },
            onAdjustStock = { newStock, _ ->
                viewModel.updateProductStock(product.id, newStock)
            }
        )
    }

    // Delete Confirmation Dialog
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = {
                Text(text = "Delete Product From Vault?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete '${prod.name}' (${prod.sku})? All associated records in SQLite will be updated.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(prod.id)
                        productToDelete = null
                    }
                ) {
                    Text("Delete", color = ShopRoseAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showBarcodeScanner) {
        CameraBarcodeScannerDialog(
            availableProducts = uiState.allProducts,
            onBarcodeScanned = { scannedCode ->
                searchQuery = scannedCode
            },
            onDismiss = { showBarcodeScanner = false }
        )
    }
}

@Composable
private fun InventoryProductCard(
    product: ProductEntity,
    currency: String,
    onEditClick: () -> Unit,
    onAdjustStockClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expandedDetails by remember { mutableStateOf(false) }
    val isLow = product.isLowStock
    val isOut = product.isOutOfStock

    val cardGlow = when {
        isOut -> ShopRoseAccent
        isLow -> ShopAmberAccent
        else -> ShopPrimary
    }

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.sku}"),
        glowColor = cardGlow,
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Image / 3D Avatar
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductVisualBadge(
                        imageUri = product.imageUri,
                        category = product.category,
                        size = 52.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = product.sku,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ShopCyanAccent
                            )

                            if (product.barcode.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = product.barcode,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = product.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (product.batchNumber.isNotBlank()) {
                                Text(
                                    text = "• Batch: ${product.batchNumber}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = ShopCyanAccent
                                )
                            }
                        }

                        if (product.expiryDate != null) {
                            Spacer(modifier = Modifier.height(3.dp))
                            val isExp = product.isExpired
                            val isSoon = product.isExpiringSoon
                            val expColor = if (isExp) ShopRoseAccent else if (isSoon) ShopAmberAccent else ShopEmeraldAccent
                            val daysLeft = ((product.expiryDate - System.currentTimeMillis()) / (24 * 3600 * 1000L)).coerceAtLeast(0)
                            val expText = if (isExp) "EXPIRED" else if (isSoon) "Exp: ${daysLeft}d left" else "Exp: ${daysLeft}d"

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(expColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = expText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = expColor
                                )
                            }
                        }
                    }
                }

                // Stock Badge
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isOut -> ShopRoseAccent.copy(alpha = 0.22f)
                                    isLow -> ShopAmberAccent.copy(alpha = 0.22f)
                                    else -> ShopEmeraldAccent.copy(alpha = 0.18f)
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${product.stockQuantity} units",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isOut -> ShopRoseAccent
                                isLow -> ShopAmberAccent
                                else -> ShopEmeraldAccent
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Min: ${product.minStockThreshold}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pricing & Margins Summary Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Selling Price",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = KarobarFormatter.formatCurrency(product.sellingPrice, currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ShopEmeraldAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Purchase Price",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = KarobarFormatter.formatCurrency(product.purchasePrice, currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(modifier = Modifier.weight(0.85f)) {
                    Text(
                        text = "Margin",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", product.profitMarginPercent)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ShopCyanAccent,
                        maxLines = 1
                    )
                }
            }

            // Expand description toggle if description exists
            if (product.description.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedDetails = !expandedDetails }
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expandedDetails) "Hide Details ▲" else "View Details ▼",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ShopPrimary
                    )
                }
            }

            // Expandable Description
            AnimatedVisibility(
                visible = expandedDetails && product.description.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row: Adjust Stock, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Adjust Stock Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ShopCyanAccent.copy(alpha = 0.15f))
                        .border(1.dp, ShopCyanAccent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onAdjustStockClick)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("btn_adjust_stock_${product.sku}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = ShopCyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Adjust Stock",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ShopCyanAccent
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Edit Product Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = onEditClick)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("btn_edit_${product.sku}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Delete Product Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ShopRoseAccent.copy(alpha = 0.15f))
                            .clickable(onClick = onDeleteClick)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("btn_delete_${product.sku}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = ShopRoseAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
