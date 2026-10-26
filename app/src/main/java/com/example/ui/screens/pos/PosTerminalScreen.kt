package com.example.ui.screens.pos

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.ui.components.CameraBarcodeScannerDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.localization.KarobarFormatter
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.ui.components.FuturisticButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.ProductVisualBadge
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.theme.ShopAmberAccent
import com.example.ui.theme.ShopBorderDark
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopRoseAccent
import com.example.ui.theme.ShopSecondary
import java.util.Locale
import kotlin.random.Random

data class CartLineItem(
    val product: ProductEntity,
    var quantity: Int
)

@Composable
fun PosTerminalScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val cartItems = remember { mutableStateMapOf<Long, CartLineItem>() }

    var discountPercent by remember { mutableDoubleStateOf(0.0) }
    var taxRate by remember { mutableDoubleStateOf(8.25) }

    var isCheckoutDialogOpen by remember { mutableStateOf(false) }
    var confirmedSale by remember { mutableStateOf<SaleEntity?>(null) }
    var confirmedSaleItems by remember { mutableStateOf<List<SaleItemEntity>>(emptyList()) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    val currency = uiState.settings.currencySymbol
    val isUrdu = uiState.settings.language == "ur"

    val allCategories = remember(uiState.allProducts) {
        listOf("All") + uiState.allProducts.map { it.category }.distinct()
    }

    val filteredCatalog = remember(uiState.allProducts, searchQuery, selectedCategory) {
        uiState.allProducts.filter { product ->
            val matchesQuery = searchQuery.isBlank() ||
                    product.name.contains(searchQuery, ignoreCase = true) ||
                    product.sku.contains(searchQuery, ignoreCase = true) ||
                    product.barcode.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || product.category.equals(selectedCategory, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }

    // Calculations
    val subtotal = remember(cartItems.values.toList()) {
        cartItems.values.sumOf { it.product.sellingPrice * it.quantity }
    }
    val discountAmount = remember(subtotal, discountPercent) {
        subtotal * (discountPercent / 100.0)
    }
    val netBeforeTax = (subtotal - discountAmount).coerceAtLeast(0.0)
    val taxAmount = remember(netBeforeTax, taxRate) {
        netBeforeTax * (taxRate / 100.0)
    }
    val grandTotal = remember(netBeforeTax, taxAmount) {
        netBeforeTax + taxAmount
    }
    val totalCartUnits = remember(cartItems.values.toList()) {
        cartItems.values.sumOf { it.quantity }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .testTag("pos_terminal_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title Bar - Clean, professional, single title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUrdu) "بلنگ / POS" else "POS",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (cartItems.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ShopRoseAccent.copy(alpha = 0.15f))
                            .clickable { cartItems.clear() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isUrdu) "ٹوکری خالی کریں" else "Clear Cart",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ShopRoseAccent
                        )
                    }
                }
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
                            modifier = Modifier.testTag("btn_pos_camera_scan")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Camera Barcode Scanner",
                                tint = ShopCyanAccent
                            )
                        }
                    }
                },
                placeholder = { Text("Search catalog or scan barcode...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pos_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShopPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Category Filter Chips
        item {
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

        // Available Products Quick Add Section
        item {
            Text(
                text = "Catalog Inventory (Tap to Add)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCatalog, key = { it.id }) { product ->
                    val inCartQty = cartItems[product.id]?.quantity ?: 0
                    val isOutOfStock = product.stockQuantity <= 0
                    val isMaxStockReached = inCartQty >= product.stockQuantity

                    Box(
                        modifier = Modifier
                            .width(170.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .clickable(enabled = !isOutOfStock && !isMaxStockReached) {
                                val current = cartItems[product.id]
                                if (current == null) {
                                    cartItems[product.id] = CartLineItem(product, 1)
                                } else if (current.quantity < product.stockQuantity) {
                                    cartItems[product.id] = current.copy(quantity = current.quantity + 1)
                                }
                            }
                            .padding(12.dp)
                            .testTag("pos_catalog_item_${product.sku}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProductVisualBadge(
                                    imageUri = product.imageUri,
                                    category = product.category,
                                    size = 40.dp
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when {
                                                isOutOfStock -> ShopRoseAccent.copy(alpha = 0.2f)
                                                product.isLowStock -> ShopAmberAccent.copy(alpha = 0.2f)
                                                else -> ShopEmeraldAccent.copy(alpha = 0.18f)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${product.stockQuantity} left",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isOutOfStock -> ShopRoseAccent
                                            product.isLowStock -> ShopAmberAccent
                                            else -> ShopEmeraldAccent
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = product.sku,
                                style = MaterialTheme.typography.labelSmall,
                                color = ShopCyanAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = KarobarFormatter.formatCurrency(product.sellingPrice, currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = ShopEmeraldAccent
                                )

                                if (inCartQty > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(ShopPrimary)
                                            .size(22.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$inCartQty",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cart Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = ShopPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Order Cart ($totalCartUnits units)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (cartItems.isEmpty()) {
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Shopping Cart Is Empty",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap any product above to add it to this invoice",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(cartItems.values.toList(), key = { it.product.id }) { lineItem ->
                val prod = lineItem.product
                val isAtMaxStock = lineItem.quantity >= prod.stockQuantity

                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = ShopPrimary,
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            Text(
                                text = prod.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${KarobarFormatter.formatCurrency(prod.sellingPrice, currency)} each • SKU: ${prod.sku}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isAtMaxStock) {
                                Text(
                                    text = "Max vault stock reached (${prod.stockQuantity})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = ShopAmberAccent
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Decrement
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        if (lineItem.quantity > 1) {
                                            cartItems[prod.id] = lineItem.copy(quantity = lineItem.quantity - 1)
                                        } else {
                                            cartItems.remove(prod.id)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "${lineItem.quantity}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Increment
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isAtMaxStock) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else ShopPrimary)
                                    .clickable(enabled = !isAtMaxStock) {
                                        if (lineItem.quantity < prod.stockQuantity) {
                                            cartItems[prod.id] = lineItem.copy(quantity = lineItem.quantity + 1)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = if (isAtMaxStock) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Subtotal
                            Text(
                                text = KarobarFormatter.formatCurrency(prod.sellingPrice * lineItem.quantity, currency),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ShopEmeraldAccent,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            // Remove
                            IconButton(
                                onClick = { cartItems.remove(prod.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Remove",
                                    tint = ShopRoseAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Discount & Tax Configuration Card
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Discount Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Apply Discount",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(0.0, 5.0, 10.0, 15.0, 20.0).forEach { disc ->
                                    val isSelected = discountPercent == disc
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) ShopCyanAccent else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { discountPercent = disc }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (disc == 0.0) "None" else "${disc.toInt()}%",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Tax Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tax Configuration",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(0.0, 5.0, 8.25, 10.0).forEach { rate ->
                                    val isSelected = taxRate == rate
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) ShopPrimary else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { taxRate = rate }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (rate == 0.0) "0%" else "$rate%",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Summary Totals & Settle Action
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Subtotal ($totalCartUnits items)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = KarobarFormatter.formatCurrency(subtotal, currency),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (discountAmount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Discount (${discountPercent.toInt()}%)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ShopCyanAccent
                                )
                                Text(
                                    text = "-${KarobarFormatter.formatCurrency(discountAmount, currency)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ShopCyanAccent
                                )
                            }
                        }

                        if (taxAmount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tax (${String.format(Locale.US, "%.1f", taxRate)}%)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+${KarobarFormatter.formatCurrency(taxAmount, currency)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Due",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = KarobarFormatter.formatCurrency(grandTotal, currency),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = ShopEmeraldAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        FuturisticButton(
                            text = "Proceed to Settle (${KarobarFormatter.formatCurrency(grandTotal, currency)})",
                            icon = Icons.Default.PointOfSale,
                            onClick = { isCheckoutDialogOpen = true },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_proceed_to_settle"
                        )
                    }
                }
            }
        }
    }

    // Checkout Dialog
    if (isCheckoutDialogOpen) {
        val isUrdu = uiState.settings.language == "ur"
        PosCheckoutDialog(
            subtotal = subtotal,
            discountAmount = discountAmount,
            discountPercent = discountPercent,
            taxRate = taxRate,
            taxAmount = taxAmount,
            grandTotal = grandTotal,
            currencySymbol = currency,
            isUrdu = isUrdu,
            onDismiss = { isCheckoutDialogOpen = false },
            onProcessPayment = { method, cashPaid, change, clientName, note ->
                // Generate unique invoice number: INV-2026-[RANDOM 4 DIGITS]
                val randomNum = Random.nextInt(1000, 9999)
                val invoiceNumber = "INV-2026-$randomNum"

                val saleEntity = SaleEntity(
                    invoiceNumber = invoiceNumber,
                    subtotal = subtotal,
                    discountAmount = discountAmount,
                    discountPercent = discountPercent,
                    taxRate = taxRate,
                    taxAmount = taxAmount,
                    totalAmount = grandTotal,
                    itemCount = totalCartUnits,
                    paymentMethod = method,
                    cashTendered = cashPaid,
                    changeDue = change,
                    customerName = clientName,
                    customerNote = note,
                    timestamp = System.currentTimeMillis()
                )

                val saleItemsList = cartItems.values.map { line ->
                    SaleItemEntity(
                        saleId = 0,
                        productId = line.product.id,
                        productName = line.product.name,
                        quantity = line.quantity,
                        unitPrice = line.product.sellingPrice,
                        subtotal = line.product.sellingPrice * line.quantity,
                        costPrice = line.product.purchasePrice
                    )
                }

                // Execute atomic sale and stock deduction in Room Database
                try {
                    viewModel.recordSale(saleEntity, saleItemsList)
                    confirmedSale = saleEntity
                    confirmedSaleItems = saleItemsList
                    cartItems.clear()
                    isCheckoutDialogOpen = false
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    // Checkout Confirmation Dialog
    confirmedSale?.let { sale ->
        CheckoutConfirmationDialog(
            sale = sale,
            items = confirmedSaleItems,
            storeName = uiState.settings.storeName,
            currencySymbol = currency,
            invoiceFooter = uiState.settings.invoiceFooter,
            businessPhone = uiState.settings.businessPhone,
            businessAddress = uiState.settings.businessAddress,
            businessNtn = uiState.settings.businessNtn,
            invoiceTemplate = uiState.settings.invoiceTemplate,
            showTaxNumber = uiState.settings.invoiceShowTaxNumber,
            showCustomerPhone = uiState.settings.invoiceShowCustomerPhone,
            showShopAddress = uiState.settings.invoiceShowShopAddress,
            onDismiss = {
                confirmedSale = null
                confirmedSaleItems = emptyList()
            }
        )
    }

    if (showBarcodeScanner) {
        CameraBarcodeScannerDialog(
            availableProducts = uiState.allProducts,
            onBarcodeScanned = { scannedCode ->
                val matchedProduct = uiState.allProducts.find {
                    it.barcode.equals(scannedCode, ignoreCase = true) ||
                            it.sku.equals(scannedCode, ignoreCase = true)
                }
                if (matchedProduct != null) {
                    val currentQty = cartItems[matchedProduct.id]?.quantity ?: 0
                    if (currentQty < matchedProduct.stockQuantity) {
                        cartItems[matchedProduct.id] = CartLineItem(matchedProduct, currentQty + 1)
                        android.widget.Toast.makeText(context, "Added: ${matchedProduct.name}", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Stock limit reached for ${matchedProduct.name}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    searchQuery = scannedCode
                    android.widget.Toast.makeText(context, "Scanned: $scannedCode", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showBarcodeScanner = false }
        )
    }
}

