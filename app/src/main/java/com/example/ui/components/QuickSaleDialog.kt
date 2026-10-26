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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.localization.KarobarFormatter
import com.example.localization.KarobarMath
import com.example.localization.KarobarPaymentMethods
import com.example.ui.theme.ShopBorderDark
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopSecondary

@Composable
fun QuickSaleDialog(
    products: List<ProductEntity>,
    currencySymbol: String = "Rs.",
    isUrdu: Boolean = false,
    onDismiss: () -> Unit,
    onRecordSale: (SaleEntity, List<SaleItemEntity>) -> Unit
) {
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var quantity by remember { mutableIntStateOf(1) }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val unitPrice = selectedProduct?.sellingPrice ?: 0.0
    val totalAmount = KarobarMath.multiply(unitPrice, quantity.toDouble())

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUrdu) "فوری بل / فروخت" else "New Quick Sale",
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

                Spacer(modifier = Modifier.height(12.dp))

                // Product selection
                Text(
                    text = if (isUrdu) "پراڈکٹ منتخب کریں" else "Select Item",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(products) { product ->
                        val isSelected = selectedProduct?.id == product.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) ShopPrimary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) ShopPrimary else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedProduct = product
                                    if (quantity > product.stockQuantity && product.stockQuantity > 0) {
                                        quantity = product.stockQuantity
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
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
                                        text = product.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${product.category} • ${if (isUrdu) "اسٹاک" else "Stock"}: ${product.stockQuantity}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (product.stockQuantity <= product.minStockThreshold) ShopSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = KarobarFormatter.formatCurrency(product.sellingPrice, currencySymbol),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ShopEmeraldAccent
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUrdu) "تعداد (مقدار)" else "Quantity Units",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { if (quantity > 1) quantity-- },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    val maxStock = selectedProduct?.stockQuantity ?: 99
                                    if (quantity < maxStock) quantity++
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selectable Pakistani Payment Methods
                Text(
                    text = if (isUrdu) "طریقہ ادائیگی" else "Payment Method (Recording)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(KarobarPaymentMethods) { method ->
                        val isSelected = paymentMethod == method.code
                        val icon = when (method.code) {
                            "CASH" -> Icons.Default.Payments
                            "BANK" -> Icons.Default.AccountBalance
                            "JAZZCASH" -> Icons.Default.PhoneAndroid
                            "EASYPAISA" -> Icons.Default.PhoneAndroid
                            "CREDIT" -> Icons.Default.AccountBalanceWallet
                            else -> Icons.Default.MoreHoriz
                        }
                        val accent = Color(method.colorHex)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) accent.copy(alpha = 0.22f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) accent else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { paymentMethod = method.code }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = method.nameEn,
                                    tint = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isUrdu) method.nameUr else method.nameEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Total display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ShopEmeraldAccent.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUrdu) "کل رقم" else "Total Due",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = KarobarFormatter.formatCurrency(totalAmount, currencySymbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ShopEmeraldAccent
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                FuturisticButton(
                    text = if (isUrdu) "بل درج کریں (${KarobarFormatter.formatCurrency(totalAmount, currencySymbol)})" else "Confirm & Record (${KarobarFormatter.formatCurrency(totalAmount, currencySymbol)})",
                    icon = Icons.Default.ShoppingCartCheckout,
                    onClick = {
                        val product = selectedProduct
                        if (product == null) {
                            errorMessage = if (isUrdu) "براہ کرم پراڈکٹ منتخب کریں" else "Please choose a product"
                            return@FuturisticButton
                        }

                        if (product.stockQuantity < quantity) {
                            errorMessage = if (isUrdu) "اسٹاک میں ناکافی تعداد" else "Insufficient stock available"
                            return@FuturisticButton
                        }

                        val sale = SaleEntity(
                            invoiceNumber = "INV-${System.currentTimeMillis() % 100000}",
                            subtotal = totalAmount,
                            discountAmount = 0.0,
                            taxRate = 0.0,
                            totalAmount = totalAmount,
                            itemCount = quantity,
                            paymentMethod = paymentMethod,
                            cashTendered = if (paymentMethod == "CASH") totalAmount else 0.0,
                            changeDue = 0.0,
                            customerName = if (isUrdu) "واک ان کسٹمر" else "Walk-in Client",
                            timestamp = System.currentTimeMillis()
                        )

                        val items = listOf(
                            SaleItemEntity(
                                productId = product.id,
                                productName = product.name,
                                quantity = quantity,
                                unitPrice = product.sellingPrice,
                                costPrice = product.costPrice,
                                subtotal = totalAmount
                            )
                        )

                        onRecordSale(sale, items)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_confirm_quick_sale"
                )
            }
        }
    }
}
