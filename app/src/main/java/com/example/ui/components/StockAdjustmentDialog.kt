package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.theme.ShopRoseAccent
import com.example.ui.theme.ShopSecondary

enum class AdjustmentType(val label: String, val description: String) {
    PURCHASE("Purchase / Stock In", "Replenish inventory from supplier delivery"),
    SHRINKAGE("Damage / Shrinkage", "Write off lost, expired, or defective units"),
    AUDIT("Audit Correction", "Set exact inventory count after physical audit")
}

@Composable
fun StockAdjustmentDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onAdjustStock: (newStock: Int, note: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(AdjustmentType.PURCHASE) }
    var quantityText by remember { mutableStateOf("10") }
    var reasonNote by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentStock = product.stockQuantity
    val qtyInput = quantityText.toIntOrNull() ?: 0

    val calculatedNewStock = when (selectedType) {
        AdjustmentType.PURCHASE -> currentStock + qtyInput
        AdjustmentType.SHRINKAGE -> (currentStock - qtyInput).coerceAtLeast(0)
        AdjustmentType.AUDIT -> qtyInput.coerceAtLeast(0)
    }

    val isResultingInLowStock = calculatedNewStock <= product.minStockThreshold

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                .padding(20.dp)
                .testTag("stock_adjustment_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ShopCyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = ShopCyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Adjust Inventory",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Adjustment Type Selector
                Text(
                    text = "Adjustment Reason",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdjustmentType.values().forEach { type ->
                        val isSelected = selectedType == type
                        val accentColor = when (type) {
                            AdjustmentType.PURCHASE -> ShopEmeraldAccent
                            AdjustmentType.SHRINKAGE -> ShopRoseAccent
                            AdjustmentType.AUDIT -> ShopCyanAccent
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) accentColor.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) accentColor else Color.Transparent,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedType = type
                                    if (type == AdjustmentType.AUDIT && quantityText.isBlank()) {
                                        quantityText = currentStock.toString()
                                    }
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = type.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = type.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(accentColor)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quantity Input
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        quantityText = it
                        errorMessage = null
                    },
                    label = {
                        Text(
                            if (selectedType == AdjustmentType.AUDIT) "New Total Count"
                            else "Quantity to ${if (selectedType == AdjustmentType.PURCHASE) "Add" else "Deduct"}"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_adjust_quantity"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShopPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Reason Note
                OutlinedTextField(
                    value = reasonNote,
                    onValueChange = { reasonNote = it },
                    label = { Text("Audit Note / Vendor Invoice (Optional)") },
                    placeholder = { Text("e.g., PO #4489 from CyberSupply") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_adjust_note"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShopPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Before and After Stock Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Stock",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currentStock units",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "➔",
                            style = MaterialTheme.typography.titleLarge,
                            color = ShopCyanAccent
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Resulting Stock",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$calculatedNewStock units",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = if (isResultingInLowStock) ShopRoseAccent else ShopEmeraldAccent
                            )
                        }
                    }
                }

                if (isResultingInLowStock) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = ShopRoseAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Warning: Resulting stock will trigger low-stock alert (Min: ${product.minStockThreshold})",
                            style = MaterialTheme.typography.labelSmall,
                            color = ShopRoseAccent
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                FuturisticButton(
                    text = "Confirm Vault Adjustment",
                    onClick = {
                        val qty = quantityText.toIntOrNull()
                        if (qty == null || qty < 0) {
                            errorMessage = "Please enter a valid quantity"
                            return@FuturisticButton
                        }
                        if (selectedType == AdjustmentType.SHRINKAGE && qty > currentStock) {
                            errorMessage = "Cannot deduct more than current stock ($currentStock units)"
                            return@FuturisticButton
                        }

                        onAdjustStock(calculatedNewStock, reasonNote.trim())
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "submit_stock_adjustment"
                )
            }
        }
    }
}
