package com.example.ui.screens.pos

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ShoppingCartCheckout
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.localization.KarobarFormatter
import com.example.localization.KarobarMath
import com.example.localization.Strings
import com.example.ui.components.FuturisticButton
import com.example.ui.theme.ShopBorderDark
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopRoseAccent
import com.example.ui.theme.ShopSecondary
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.ceil

data class PakistaniPaymentMethod(
    val id: String,
    val nameEn: String,
    val nameUr: String,
    val icon: ImageVector,
    val accentColor: Color
)

val PakistaniPaymentOptions = listOf(
    PakistaniPaymentMethod("CASH", "Cash", "نقد (کیش)", Icons.Default.Payments, ShopEmeraldAccent),
    PakistaniPaymentMethod("BANK", "Bank Transfer", "بینک ٹرانسفر", Icons.Default.AccountBalance, ShopPrimary),
    PakistaniPaymentMethod("JAZZCASH", "JazzCash", "جاز کیش", Icons.Default.PhoneAndroid, Color(0xFFFF5722)),
    PakistaniPaymentMethod("EASYPAISA", "Easypaisa", "ایزی پیسہ", Icons.Default.PhoneAndroid, Color(0xFF00C853)),
    PakistaniPaymentMethod("CREDIT", "Credit / Udhaar", "ادھار کھاتہ", Icons.Default.AccountBalanceWallet, ShopCyanAccent),
    PakistaniPaymentMethod("OTHER", "Other", "دیگر", Icons.Default.MoreHoriz, ShopSecondary)
)

@Composable
fun PosCheckoutDialog(
    subtotal: Double,
    discountAmount: Double,
    discountPercent: Double,
    taxRate: Double,
    taxAmount: Double,
    grandTotal: Double,
    currencySymbol: String,
    isUrdu: Boolean = false,
    onDismiss: () -> Unit,
    onProcessPayment: (
        paymentMethod: String,
        cashTendered: Double,
        changeDue: Double,
        customerName: String,
        customerNote: String
    ) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("CASH") }
    var cashInput by remember {
        mutableStateOf(String.format(Locale.US, "%.0f", grandTotal))
    }
    var transactionRef by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerNote by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val tendered = cashInput.toDoubleOrNull() ?: 0.0
    val changeDue = KarobarMath.subtract(tendered, grandTotal).coerceAtLeast(0.0)

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                .padding(20.dp)
                .testTag("pos_checkout_dialog")
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ShopPrimary.copy(alpha = 0.15f))
                                .border(1.dp, ShopPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCartCheckout,
                                contentDescription = null,
                                tint = ShopPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isUrdu) "ادائیگی اور بل فائنل کریں" else "Settle & Checkout",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown Overview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Strings.subtotal(isUrdu),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = KarobarFormatter.formatCurrency(subtotal, currencySymbol),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (discountAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${Strings.discount(isUrdu)} (${String.format(Locale.US, "%.0f", discountPercent)}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ShopCyanAccent
                                )
                                Text(
                                    text = "-${KarobarFormatter.formatCurrency(discountAmount, currencySymbol)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ShopCyanAccent
                                )
                            }
                        }

                        if (taxAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${Strings.tax(isUrdu)} (${String.format(Locale.US, "%.1f", taxRate)}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+${KarobarFormatter.formatCurrency(taxAmount, currencySymbol)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Strings.totalDue(isUrdu),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = KarobarFormatter.formatCurrency(grandTotal, currencySymbol),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = ShopEmeraldAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Methods Selection (Pakistani Options)
                Text(
                    text = if (isUrdu) "ادائیگی کا طریقہ منتخب کریں" else "Select Payment Method",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PakistaniPaymentOptions) { option ->
                        val isSelected = selectedMethod == option.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) option.accentColor.copy(alpha = 0.22f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    1.2.dp,
                                    if (isSelected) option.accentColor else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedMethod = option.id
                                    errorMessage = null
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) option.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isUrdu) option.nameUr else option.nameEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) option.accentColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Specific Controls
                when (selectedMethod) {
                    "CASH" -> {
                        // Quick Cash Tender Options for Pakistan: Exact, +100, +500, +1000, +5000
                        Text(
                            text = if (isUrdu) "فوری کیش رقم" else "Quick Cash Tender",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val exact = grandTotal
                        val nextHundred = ceil(grandTotal / 100.0) * 100.0
                        val nextFiveHundred = ceil(grandTotal / 500.0) * 500.0
                        val nextThousand = ceil(grandTotal / 1000.0) * 1000.0
                        val nextFiveThousand = ceil(grandTotal / 5000.0) * 5000.0

                        val quickChips = listOf(exact, nextHundred, nextFiveHundred, nextThousand, nextFiveThousand)
                            .distinct()
                            .filter { it >= grandTotal }
                            .take(4)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickChips.forEach { amount ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ShopEmeraldAccent.copy(alpha = 0.18f))
                                        .border(1.dp, ShopEmeraldAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .clickable {
                                            cashInput = String.format(Locale.US, "%.0f", amount)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = KarobarFormatter.formatCurrency(amount, currencySymbol),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ShopEmeraldAccent
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = cashInput,
                            onValueChange = {
                                cashInput = it
                                errorMessage = null
                            },
                            label = { Text(Strings.cashReceived(isUrdu)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Change Due Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (tendered >= grandTotal) ShopEmeraldAccent.copy(alpha = 0.15f)
                                    else ShopRoseAccent.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (tendered >= grandTotal) Strings.changeDue(isUrdu) else (if (isUrdu) "رقم کم ہے" else "Underpaid by"),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (tendered >= grandTotal) ShopEmeraldAccent else ShopRoseAccent
                            )
                            Text(
                                text = KarobarFormatter.formatCurrency(
                                    if (tendered >= grandTotal) changeDue else grandTotal - tendered,
                                    currencySymbol
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = if (tendered >= grandTotal) ShopEmeraldAccent else ShopRoseAccent
                            )
                        }
                    }

                    "BANK", "JAZZCASH", "EASYPAISA" -> {
                        OutlinedTextField(
                            value = transactionRef,
                            onValueChange = { transactionRef = it },
                            label = { Text(if (isUrdu) "ٹرانزیکشن آئی ڈی / ریفرنس نمبر" else "Transaction ID / Reference (TID)") },
                            placeholder = { Text("e.g., TID-889410294") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    "CREDIT" -> {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = {
                                customerName = it
                                errorMessage = null
                            },
                            label = { Text(if (isUrdu) "گاہک کا نام (ادھار کھاتہ لازمی) *" else "Customer Name (Required for Udhaar) *") },
                            placeholder = { Text(if (isUrdu) "مثلاً: چوہدری بلال احمد" else "e.g. Bilal Ahmed") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text(if (isUrdu) "موبائل فون (03XXXXXXXXX)" else "Customer Phone (03XXXXXXXXX)") },
                            placeholder = { Text("03001234567") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    "OTHER" -> {
                        Text(
                            text = if (isUrdu) "کسی اور ذریعے سے ادائیگی موصول کی جا رہی ہے" else "Recording miscellaneous offline settlement",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Customer Note
                OutlinedTextField(
                    value = customerNote,
                    onValueChange = { customerNote = it },
                    label = { Text(if (isUrdu) "انوائس یا بل پر نوٹ (اختیاری)" else "Invoice Note (Optional)") },
                    placeholder = { Text(if (isUrdu) "خصوصی کسٹمر نوٹ یا رعایت..." else "Order notes, dispatch terms...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Final Button
                FuturisticButton(
                    text = "${if (isUrdu) "بل محفوظ کریں" else "Confirm & Settle"} (${KarobarFormatter.formatCurrency(grandTotal, currencySymbol)})",
                    onClick = {
                        if (selectedMethod == "CASH" && tendered < grandTotal) {
                            errorMessage = if (isUrdu) "وصول شدہ کیش کل رقم سے کم ہے" else "Cash received is less than total due"
                            return@FuturisticButton
                        }

                        if (selectedMethod == "CREDIT" && customerName.isBlank()) {
                            errorMessage = if (isUrdu) "ادھار کھاتہ کے لیے کسٹمر کا نام ضروری ہے" else "Customer name is required for credit/udhaar"
                            return@FuturisticButton
                        }

                        if (customerPhone.isNotBlank() && !KarobarFormatter.isValidPakistaniPhone(customerPhone)) {
                            errorMessage = if (isUrdu) "غلط موبائل نمبر۔ درست پاکستانی موبائل نمبر درج کریں (مثلاً 03001234567 یا 923001234567+)"
                            else "Invalid phone number. Must be a valid Pakistani mobile number (e.g. 03001234567 or +923001234567)"
                            return@FuturisticButton
                        }

                        val finalNote = buildString {
                            if (customerNote.isNotBlank()) append(customerNote)
                            if (transactionRef.isNotBlank()) {
                                if (isNotEmpty()) append(" • ")
                                append("Ref: $transactionRef")
                            }
                            if (customerPhone.isNotBlank()) {
                                if (isNotEmpty()) append(" • ")
                                append("Phone: ${KarobarFormatter.normalizePakistaniPhone(customerPhone)}")
                            }
                        }

                        val selectedOption = PakistaniPaymentOptions.find { it.id == selectedMethod }
                        val paymentLabel = if (isUrdu) selectedOption?.nameUr ?: selectedMethod else selectedOption?.nameEn ?: selectedMethod

                        onProcessPayment(
                            paymentLabel,
                            if (selectedMethod == "CASH") tendered else grandTotal,
                            if (selectedMethod == "CASH") changeDue else 0.0,
                            customerName.trim(),
                            finalNote
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_confirm_checkout_settle"
                )
            }
        }
    }
}
