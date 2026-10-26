package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.screens.dashboard.DashboardMetrics
import com.example.ui.theme.ShopBorderDark
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExportReportDialog(
    storeName: String,
    currencySymbol: String,
    metrics: DashboardMetrics,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentDateStr = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date()) }

    val reportText = remember(metrics, storeName, currencySymbol) {
        buildString {
            appendLine("==========================================")
            appendLine("         KAROBARFLOW BUSINESS REPORT      ")
            appendLine("==========================================")
            appendLine("Store: $storeName")
            appendLine("Generated: $currentDateStr")
            appendLine("------------------------------------------")
            appendLine("FINANCIAL METRICS:")
            appendLine("  • Lifetime Revenue : $currencySymbol${String.format(Locale.US, "%,.2f", metrics.totalRevenue)}")
            appendLine("  • Gross Profit     : $currencySymbol${String.format(Locale.US, "%,.2f", metrics.grossProfit)}")
            appendLine("  • Net Profit       : $currencySymbol${String.format(Locale.US, "%,.2f", metrics.netProfit)}")
            appendLine("  • Profit Margin    : ${String.format(Locale.US, "%.1f", metrics.grossProfitMargin)}%")
            appendLine("  • Today's Revenue  : $currencySymbol${String.format(Locale.US, "%,.2f", metrics.todaySalesRevenue)}")
            appendLine("  • Average Ticket   : $currencySymbol${String.format(Locale.US, "%,.2f", metrics.averageTicket)}")
            appendLine("------------------------------------------")
            appendLine("INVENTORY & OPERATIONS:")
            appendLine("  • Active SKUs      : ${metrics.totalProducts}")
            appendLine("  • In-Stock Units   : ${metrics.totalStockUnits}")
            appendLine("  • Cost Value       : $currencySymbol${String.format(Locale.US, "%,.2f", metrics.inventoryCostValue)}")
            appendLine("  • Retail Value     : $currencySymbol${String.format(Locale.US, "%,.2f", metrics.inventoryRetailValue)}")
            appendLine("  • Low Stock Alerts : ${metrics.lowStockCount}")
            appendLine("------------------------------------------")
            appendLine("TRANSACTIONS:")
            appendLine("  • Total Orders     : ${metrics.totalSalesCount}")
            appendLine("  • Today's Orders   : ${metrics.todaySalesCount}")
            appendLine("==========================================")
            appendLine("100% Offline Local Database")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                .padding(20.dp)
                .testTag("export_report_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                                .background(ShopPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = ShopPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Business Report",
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

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = reportText,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FuturisticButton(
                        text = "Copy to Clipboard",
                        icon = Icons.Default.ContentCopy,
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ShopFlow Report", reportText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Report copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FuturisticButton(
                        text = "Close",
                        isSecondary = true,
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        }
    }
}
