package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SaleEntity
import com.example.data.repository.ShopFlowRepository
import com.example.localization.KarobarFormatter
import com.example.ui.theme.ShopBorderDark
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopSecondary
import com.example.ui.util.InvoicePdfGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceiptDialog(
    sale: SaleEntity,
    currencySymbol: String,
    repository: ShopFlowRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("MMMM d, yyyy • HH:mm:ss", Locale.US) }
    val formattedDate = remember(sale.timestamp) { dateFormat.format(Date(sale.timestamp)) }
    val itemsFlow = remember(sale.id) { repository.getSaleItems(sale.id) }
    val items by itemsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val settingsFlow = remember { repository.appSettings }
    val settings by settingsFlow.collectAsStateWithLifecycle(initialValue = null)

    val paymentIcon = when (sale.paymentMethod) {
        "CARD" -> Icons.Default.CreditCard
        "DIGITAL" -> Icons.Default.QrCode2
        else -> Icons.Default.Payments
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                .padding(20.dp)
                .testTag("receipt_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Store Branding Header
                val currentSettings = settings
                val storeTitle = currentSettings?.storeName?.takeIf { it.isNotBlank() } ?: "KarobarFlow Store"
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = storeTitle.uppercase(Locale.US),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (currentSettings?.invoiceShowShopAddress == true && !currentSettings.businessAddress.isNullOrBlank()) {
                        Text(
                            text = currentSettings.businessAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val contactLine = buildString {
                        if (currentSettings?.invoiceShowShopAddress == true && !currentSettings.businessPhone.isNullOrBlank()) {
                            append("Tel: ${currentSettings.businessPhone}")
                        }
                        if (currentSettings?.invoiceShowTaxNumber == true && !currentSettings.businessNtn.isNullOrBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append("NTN: ${currentSettings.businessNtn}")
                        }
                    }
                    if (contactLine.isNotBlank()) {
                        Text(
                            text = contactLine,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Header Bar with Invoice Number & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ShopPrimary.copy(alpha = 0.2f)),
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
                        Column {
                            val title = when (settings?.invoiceTemplate) {
                                "THERMAL" -> "POS Thermal Receipt"
                                "DETAILED" -> "Detailed Tax Invoice"
                                else -> "Tax Invoice Receipt"
                            }
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = sale.invoiceNumber,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ShopCyanAccent
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

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Timestamp",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Payment Channel",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = paymentIcon,
                                    contentDescription = null,
                                    tint = ShopCyanAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = sale.paymentMethod,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ShopCyanAccent
                                )
                            }
                        }

                        if (sale.customerName.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Customer",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = sale.customerName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (sale.customerNote.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Ledger Note",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = sale.customerNote,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Itemized Breakdown",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Direct Sale (${sale.itemCount} units)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items.forEach { item ->
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
                                        text = item.productName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.quantity}x @ ${KarobarFormatter.formatCurrency(item.unitPrice, currencySymbol)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = KarobarFormatter.formatCurrency(item.subtotal, currencySymbol),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Breakdown Totals (Subtotal, Discount, Tax, Grand Total)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Subtotal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = KarobarFormatter.formatCurrency(if (sale.subtotal > 0) sale.subtotal else sale.totalAmount, currencySymbol),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (sale.discountAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Discount (${String.format(Locale.US, "%.0f", sale.discountPercent)}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ShopCyanAccent
                                )
                                Text(
                                    text = "-${KarobarFormatter.formatCurrency(sale.discountAmount, currencySymbol)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ShopCyanAccent
                                )
                            }
                        }

                        if (sale.taxAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tax (${String.format(Locale.US, "%.1f", sale.taxRate)}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+${KarobarFormatter.formatCurrency(sale.taxAmount, currencySymbol)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (sale.paymentMethod == "CASH" && sale.cashTendered > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Cash Received",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = KarobarFormatter.formatCurrency(sale.cashTendered, currencySymbol),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Change Due",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ShopEmeraldAccent
                                )
                                Text(
                                    text = KarobarFormatter.formatCurrency(sale.changeDue, currencySymbol),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ShopEmeraldAccent
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Total Summary Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ShopEmeraldAccent.copy(alpha = 0.14f))
                        .border(1.dp, ShopEmeraldAccent.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Settled",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = KarobarFormatter.formatCurrency(sale.totalAmount, currencySymbol),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = ShopEmeraldAccent
                    )
                }

                val footerDisclaimer = settings?.invoiceFooter?.takeIf { it.isNotBlank() }
                    ?: "Thank you for your business! Computerized tax invoice generated by KarobarFlow."
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = footerDisclaimer,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Actions: WhatsApp Share & Thermal Print
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FuturisticButton(
                        text = "WhatsApp",
                        icon = Icons.Default.Share,
                        onClick = {
                            val customerPhone = if (sale.customerNote.contains("Phone:")) {
                                val idx = sale.customerNote.indexOf("Phone:")
                                sale.customerNote.substring(idx + 6).trim().take(15)
                            } else null

                            com.example.util.WhatsAppShareHelper.shareInvoiceViaWhatsApp(
                                context = context,
                                sale = sale,
                                items = items,
                                storeName = settings?.storeName ?: "Smart Shop",
                                currencySymbol = currencySymbol,
                                customerPhone = customerPhone,
                                businessPhone = settings?.businessPhone,
                                businessAddress = settings?.businessAddress
                            )
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "receipt_btn_whatsapp"
                    )

                    FuturisticButton(
                        text = "Thermal Print",
                        icon = Icons.Default.Receipt,
                        onClick = {
                            com.example.util.ThermalReceiptPrinter.printReceipt(
                                context = context,
                                sale = sale,
                                items = items,
                                storeName = settings?.storeName ?: "Smart Shop",
                                currencySymbol = currencySymbol,
                                businessPhone = settings?.businessPhone,
                                businessAddress = settings?.businessAddress,
                                businessNtn = settings?.businessNtn,
                                invoiceFooter = settings?.invoiceFooter
                            )
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "receipt_btn_print"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Actions: Share PDF & Share Text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FuturisticButton(
                        text = "PDF Invoice",
                        icon = Icons.Default.PictureAsPdf,
                        isSecondary = true,
                        onClick = {
                            InvoicePdfGenerator.shareInvoicePdf(
                                context = context,
                                sale = sale,
                                items = items,
                                storeName = settings?.storeName ?: "Smart Shop",
                                currencySymbol = currencySymbol,
                                invoiceFooter = settings?.invoiceFooter,
                                businessPhone = settings?.businessPhone,
                                businessAddress = settings?.businessAddress,
                                businessNtn = settings?.businessNtn,
                                invoiceTemplate = settings?.invoiceTemplate ?: "STANDARD",
                                showTaxNumber = settings?.invoiceShowTaxNumber ?: true,
                                showCustomerPhone = settings?.invoiceShowCustomerPhone ?: true,
                                showShopAddress = settings?.invoiceShowShopAddress ?: true
                            )
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "receipt_btn_share_pdf"
                    )

                    FuturisticButton(
                        text = "Plain Text",
                        icon = Icons.Default.Share,
                        isSecondary = true,
                        onClick = {
                            InvoicePdfGenerator.shareInvoiceAsText(
                                context = context,
                                sale = sale,
                                items = items,
                                storeName = settings?.storeName ?: "Smart Shop",
                                currencySymbol = currencySymbol,
                                invoiceFooter = settings?.invoiceFooter,
                                businessPhone = settings?.businessPhone,
                                businessAddress = settings?.businessAddress,
                                businessNtn = settings?.businessNtn,
                                invoiceTemplate = settings?.invoiceTemplate ?: "STANDARD",
                                showTaxNumber = settings?.invoiceShowTaxNumber ?: true,
                                showCustomerPhone = settings?.invoiceShowCustomerPhone ?: true,
                                showShopAddress = settings?.invoiceShowShopAddress ?: true
                            )
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "receipt_btn_share_text"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                FuturisticButton(
                    text = "Close",
                    isSecondary = true,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
