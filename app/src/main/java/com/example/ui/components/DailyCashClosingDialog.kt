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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.SaleEntity
import com.example.localization.KarobarFormatter
import com.example.ui.theme.ShopAmberAccent
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopRoseAccent
import com.example.util.WhatsAppShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyCashClosingDialog(
    sales: List<SaleEntity>,
    expenses: List<ExpenseEntity>,
    storeName: String,
    currencySymbol: String,
    isUrdu: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val todayDateStr = remember {
        SimpleDateFormat("dd MMMM yyyy", Locale.US).format(Date())
    }

    // Filter today's sales & expenses
    val now = System.currentTimeMillis()
    val startOfDay = remember(now) {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    val todaySales = remember(sales, startOfDay) {
        sales.filter { it.timestamp >= startOfDay }
    }
    val todayExpenses = remember(expenses, startOfDay) {
        expenses.filter { it.timestamp >= startOfDay }
    }

    val cashSales = remember(todaySales) {
        todaySales.filter { it.paymentMethod == "CASH" }.sumOf { it.totalAmount }
    }
    val digitalSales = remember(todaySales) {
        todaySales.filter { it.paymentMethod == "DIGITAL" }.sumOf { it.totalAmount }
    }
    val cardSales = remember(todaySales) {
        todaySales.filter { it.paymentMethod == "CARD" }.sumOf { it.totalAmount }
    }
    val cashExpenses = remember(todayExpenses) {
        todayExpenses.filter { it.paymentMethod == "CASH" }.sumOf { it.amount }
    }

    var openingCashStr by remember { mutableStateOf("5000") }
    var actualCashStr by remember {
        val defaultActual = (5000.0 + cashSales - cashExpenses).coerceAtLeast(0.0)
        mutableStateOf(String.format(Locale.US, "%.0f", defaultActual))
    }
    var shiftNotes by remember { mutableStateOf("") }

    val openingCash = openingCashStr.toDoubleOrNull() ?: 0.0
    val actualCash = actualCashStr.toDoubleOrNull() ?: 0.0
    val expectedCash = (openingCash + cashSales - cashExpenses).coerceAtLeast(0.0)
    val variance = actualCash - expectedCash

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                .padding(20.dp)
                .testTag("daily_cash_closing_dialog")
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(ShopPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PointOfSale,
                                contentDescription = null,
                                tint = ShopPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isUrdu) "روزانہ کیش کلوزنگ" else "Daily Cash Closing",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$todayDateStr • Z-Report",
                                style = MaterialTheme.typography.labelSmall,
                                color = ShopCyanAccent,
                                fontWeight = FontWeight.SemiBold
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

                // Breakdown Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isUrdu) "شفت کے اعداد و شمار" else "Register Activity Today",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Opening Cash Input
                        OutlinedTextField(
                            value = openingCashStr,
                            onValueChange = { openingCashStr = it },
                            label = { Text("Opening Cash Float ($currencySymbol)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cash Sales (${todaySales.count { it.paymentMethod == "CASH" }} sales):", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "+${KarobarFormatter.formatCurrency(cashSales, currencySymbol)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = ShopEmeraldAccent
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Digital / EasyPaisa / JazzCash:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                KarobarFormatter.formatCurrency(digitalSales, currencySymbol),
                                style = MaterialTheme.typography.bodySmall,
                                color = ShopCyanAccent
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bank / Card:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                KarobarFormatter.formatCurrency(cardSales, currencySymbol),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cash Expenses Paid:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "-${KarobarFormatter.formatCurrency(cashExpenses, currencySymbol)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = ShopRoseAccent
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)

                        // Expected Total In Drawer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Expected Cash in Drawer:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                KarobarFormatter.formatCurrency(expectedCash, currencySymbol),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = ShopPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Physical Count Input & Variance
                OutlinedTextField(
                    value = actualCashStr,
                    onValueChange = { actualCashStr = it },
                    label = { Text("Physical Cash Counted ($currencySymbol)") },
                    placeholder = { Text("Enter counted amount in cash drawer") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Variance Card
                val isBalanced = kotlin.math.abs(variance) < 1.0
                val varianceColor = when {
                    isBalanced -> ShopEmeraldAccent
                    variance > 0 -> ShopEmeraldAccent
                    else -> ShopRoseAccent
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(varianceColor.copy(alpha = 0.15f))
                        .border(1.dp, varianceColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Closing Cash Variance",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = when {
                                    isBalanced -> "Balanced / کوئی فرق نہیں"
                                    variance > 0 -> "Surplus (اضافی)"
                                    else -> "Cash Shortage (کمی)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = varianceColor
                            )
                        }
                        Text(
                            text = (if (variance > 0) "+" else "") + KarobarFormatter.formatCurrency(variance, currencySymbol),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = varianceColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Shift Note Input
                OutlinedTextField(
                    value = shiftNotes,
                    onValueChange = { shiftNotes = it },
                    label = { Text("Shift Note / Comments (Optional)") },
                    placeholder = { Text("e.g. Morning shift closed by Ali") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Share WhatsApp Z-Report & Done
                FuturisticButton(
                    text = if (isUrdu) "واٹس ایپ پر Z-Report بھیجیں" else "Share Z-Report on WhatsApp",
                    icon = Icons.Default.Share,
                    onClick = {
                        WhatsAppShareHelper.shareDailyCashClosing(
                            context = context,
                            storeName = storeName,
                            dateStr = todayDateStr,
                            openingCash = openingCash,
                            cashSales = cashSales,
                            digitalSales = digitalSales,
                            udhaarRecoveries = 0.0,
                            cashExpenses = cashExpenses,
                            expectedCash = expectedCash,
                            actualCash = actualCash,
                            variance = variance,
                            notes = shiftNotes,
                            currencySymbol = currencySymbol
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_share_z_report"
                )

                Spacer(modifier = Modifier.height(8.dp))

                FuturisticButton(
                    text = if (isUrdu) "بند کریں" else "Dismiss",
                    isSecondary = true,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
