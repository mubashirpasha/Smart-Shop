package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ProductEntity
import com.example.ui.theme.ShopAmberAccent
import com.example.ui.theme.ShopBorderDark
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.localization.BusinessCategoriesEn
import com.example.ui.theme.ShopRoseAccent
import com.example.ui.theme.ShopSecondary
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    initialProduct: ProductEntity? = null,
    existingCategories: List<String> = emptyList(),
    currencySymbol: String = "Rs.",
    onDismiss: () -> Unit,
    onSaveProduct: (ProductEntity) -> Unit
) {
    val isEditing = initialProduct != null

    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var sku by remember { mutableStateOf(initialProduct?.sku ?: "") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "General Store") }
    var customCategory by remember { mutableStateOf("") }
    var isCustomCategoryMode by remember { mutableStateOf(false) }

    var purchasePriceText by remember {
        mutableStateOf(if (initialProduct != null) String.format(Locale.US, "%.2f", initialProduct.purchasePrice) else "")
    }
    var sellingPriceText by remember {
        mutableStateOf(if (initialProduct != null) String.format(Locale.US, "%.2f", initialProduct.sellingPrice) else "")
    }
    var stockText by remember {
        mutableStateOf(initialProduct?.stockQuantity?.toString() ?: "10")
    }
    var minStockText by remember {
        mutableStateOf(initialProduct?.minStockThreshold?.toString() ?: "5")
    }
    var selectedImageUri by remember {
        mutableStateOf(initialProduct?.imageUri ?: "preset_workstation")
    }
    var batchNumber by remember { mutableStateOf(initialProduct?.batchNumber ?: "") }
    var expiryDaysText by remember {
        val days = initialProduct?.expiryDate?.let {
            val diff = (it - System.currentTimeMillis()) / (24 * 3600 * 1000L)
            diff.coerceAtLeast(0).toString()
        } ?: ""
        mutableStateOf(days)
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var stockError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    // Android Photo Picker launcher (zero-permission, compliant with Play Store Policy)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri.toString()
        }
    }

    val defaultCategoryOptions = remember(existingCategories) {
        (BusinessCategoriesEn + existingCategories).distinct().filter { it.isNotBlank() }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                .padding(20.dp)
                .testTag("product_form_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Product" else "Add New Product",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Product Image Selector Section
                Text(
                    text = "Visual Avatar / Product Image",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductVisualBadge(
                        imageUri = selectedImageUri,
                        category = if (isCustomCategoryMode) customCategory else category,
                        size = 56.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ShopPrimary.copy(alpha = 0.15f))
                            .border(1.dp, ShopPrimary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("btn_pick_photo"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = ShopPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gallery Photo",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ShopPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Or choose a high-tech 3D preset icon:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ProductVisualPresets) { preset ->
                        val isSelected = selectedImageUri == preset.id
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) preset.accentColor.copy(alpha = 0.35f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    1.2.dp,
                                    if (isSelected) preset.accentColor else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedImageUri = preset.id },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = preset.icon,
                                contentDescription = preset.label,
                                tint = if (isSelected) preset.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Product Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                        generalError = null
                    },
                    label = { Text("Product Name *") },
                    placeholder = { Text("e.g., CyberCore Workstation Pro") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_form_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShopPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Product Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Product Description") },
                    placeholder = { Text("Specifications, technical details, warranty notes...") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_form_description"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShopPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // SKU & Barcode Fields with Auto-Generators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it.uppercase() },
                            label = { Text("SKU *") },
                            placeholder = { Text("SF-CC900") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val prefix = (name.filter { it.isLetter() }.take(3).ifBlank { "PRD" }).uppercase()
                                        sku = "SF-$prefix${Random.nextInt(100, 999)}"
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoFixHigh,
                                        contentDescription = "Auto SKU",
                                        tint = ShopCyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_form_sku"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShopPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Barcode") },
                            placeholder = { Text("880923145001") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        barcode = "880" + (100000000L + Random.nextLong(899999999L))
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = "Auto Barcode",
                                        tint = ShopEmeraldAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_form_barcode"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShopPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Classification",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = if (isCustomCategoryMode) "Select Preset" else "+ Custom Category",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ShopPrimary,
                        modifier = Modifier
                            .clickable { isCustomCategoryMode = !isCustomCategoryMode }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (isCustomCategoryMode) {
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("Custom Category Name") },
                        placeholder = { Text("e.g., Optics, Robotics") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_form_custom_category"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShopPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("dropdown_form_category"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShopPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            defaultCategoryOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        category = option
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price Row: Purchase Price & Selling Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = purchasePriceText,
                        onValueChange = {
                            purchasePriceText = it
                            priceError = null
                        },
                        label = { Text("Purchase Price ($currencySymbol)") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_form_purchase_price"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShopPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = {
                            sellingPriceText = it
                            priceError = null
                        },
                        label = { Text("Selling Price ($currencySymbol) *") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = priceError != null,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_form_selling_price"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShopPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                if (priceError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = priceError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stock Row: Stock Quantity & Minimum Stock Level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = {
                            stockText = it
                            stockError = null
                        },
                        label = { Text("Stock Quantity *") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_form_stock"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShopPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it },
                        label = { Text("Min Stock Level *") },
                        placeholder = { Text("5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_form_min_stock"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShopPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                if (stockError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stockError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Batch & Expiry Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = batchNumber,
                        onValueChange = { batchNumber = it },
                        label = { Text("Batch / Lot # (Optional)") },
                        placeholder = { Text("e.g. LOT-2026A") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_form_batch"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShopPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = expiryDaysText,
                        onValueChange = { expiryDaysText = it },
                        label = { Text("Expires in Days") },
                        placeholder = { Text("e.g. 90") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_form_expiry_days"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShopPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // General Error Message
                if (generalError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = generalError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                FuturisticButton(
                    text = if (isEditing) "Save Product Changes" else "Store In Vault",
                    icon = Icons.Default.Save,
                    onClick = {
                        // Strict validation
                        val trimmedName = name.trim()
                        if (trimmedName.isBlank()) {
                            nameError = "Product name is required"
                            return@FuturisticButton
                        }

                        val sellingPrice = sellingPriceText.toDoubleOrNull()
                        if (sellingPrice == null || sellingPrice <= 0.0) {
                            priceError = "Enter a valid selling price greater than 0"
                            return@FuturisticButton
                        }

                        val purchasePrice = purchasePriceText.toDoubleOrNull() ?: 0.0
                        if (purchasePrice < 0.0) {
                            priceError = "Purchase price cannot be negative"
                            return@FuturisticButton
                        }

                        val stock = stockText.toIntOrNull()
                        if (stock == null || stock < 0) {
                            stockError = "Stock quantity must be 0 or higher"
                            return@FuturisticButton
                        }

                        val minStock = minStockText.toIntOrNull() ?: 5
                        if (minStock < 0) {
                            stockError = "Minimum stock level cannot be negative"
                            return@FuturisticButton
                        }

                        val finalCategory = if (isCustomCategoryMode && customCategory.isNotBlank()) {
                            customCategory.trim()
                        } else category.trim().ifBlank { "General" }

                        val finalSku = if (sku.isBlank()) {
                            val prefix = (trimmedName.filter { it.isLetter() }.take(3).ifBlank { "PRD" }).uppercase()
                            "SF-$prefix${Random.nextInt(100, 999)}"
                        } else sku.trim().uppercase()

                        val finalBarcode = if (barcode.isBlank()) {
                            "880" + (100000000L + Random.nextLong(899999999L))
                        } else barcode.trim()

                        val expiryDate = expiryDaysText.toLongOrNull()?.let { days ->
                            if (days > 0) System.currentTimeMillis() + (days * 24 * 3600 * 1000L) else null
                        } ?: initialProduct?.expiryDate

                        val product = ProductEntity(
                            id = initialProduct?.id ?: 0,
                            name = trimmedName,
                            description = description.trim(),
                            sku = finalSku,
                            barcode = finalBarcode,
                            category = finalCategory,
                            purchasePrice = purchasePrice,
                            sellingPrice = sellingPrice,
                            stockQuantity = stock,
                            minStockThreshold = minStock,
                            imageUri = selectedImageUri,
                            isFeatured = initialProduct?.isFeatured ?: false,
                            createdAt = initialProduct?.createdAt ?: System.currentTimeMillis(),
                            expiryDate = expiryDate,
                            batchNumber = batchNumber.trim()
                        )

                        onSaveProduct(product)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "submit_product_form"
                )
            }
        }
    }
}
