package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.localization.KarobarFormatter
import java.net.URLEncoder
import java.util.Locale

object WhatsAppShareHelper {

    /**
     * Normalizes a phone number for WhatsApp URL scheme (E.164 without plus).
     * Handles Pakistani formats like:
     * "0300 1234567" -> "923001234567"
     * "+92 300 1234567" -> "923001234567"
     * "0321-5551234" -> "923215551234"
     */
    fun cleanPhoneNumber(rawPhone: String?): String {
        if (rawPhone.isNullOrBlank()) return ""
        val digits = rawPhone.replace(Regex("[^0-9]"), "")
        return when {
            digits.startsWith("03") && digits.length >= 11 -> "92" + digits.substring(1)
            digits.startsWith("92") -> digits
            digits.startsWith("0092") -> digits.substring(2)
            else -> digits
        }
    }

    /**
     * Builds a beautifully formatted WhatsApp invoice text with emojis and clear line items.
     */
    fun buildWhatsAppInvoiceText(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        storeName: String,
        currencySymbol: String,
        businessPhone: String? = null,
        businessAddress: String? = null
    ): String {
        val dateStr = KarobarFormatter.formatDateTime(sale.timestamp)
        return buildString {
            appendLine("🏬 *${storeName.uppercase(Locale.US)}*")
            if (!businessAddress.isNullOrBlank()) {
                appendLine("📍 $businessAddress")
            }
            if (!businessPhone.isNullOrBlank()) {
                appendLine("📞 $businessPhone")
            }
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("🧾 *TAX INVOICE / رسید*: `${sale.invoiceNumber}`")
            appendLine("📅 *Date*: $dateStr (PKT)")
            appendLine("💳 *Payment*: ${sale.paymentMethod}")
            if (sale.customerName.isNotBlank()) {
                appendLine("👤 *Customer*: ${sale.customerName}")
            }
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("*ITEMS PURCHASED / خریدی گئی اشیاء:*")
            items.forEachIndexed { idx, item ->
                appendLine("${idx + 1}. *${item.productName}*")
                appendLine("   ${item.quantity}x @ ${KarobarFormatter.formatCurrency(item.unitPrice, currencySymbol)} = *${KarobarFormatter.formatCurrency(item.subtotal, currencySymbol)}*")
            }
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("▫️ Subtotal: ${KarobarFormatter.formatCurrency(sale.subtotal, currencySymbol)}")
            if (sale.discountAmount > 0) {
                appendLine("🏷️ Discount (${String.format(Locale.US, "%.0f", sale.discountPercent)}%): -${KarobarFormatter.formatCurrency(sale.discountAmount, currencySymbol)}")
            }
            if (sale.taxAmount > 0) {
                appendLine("🏛️ Tax (${String.format(Locale.US, "%.1f", sale.taxRate)}%): +${KarobarFormatter.formatCurrency(sale.taxAmount, currencySymbol)}")
            }
            appendLine("💰 *GRAND TOTAL / کل رقم*: *${KarobarFormatter.formatCurrency(sale.totalAmount, currencySymbol)}*")
            if (sale.paymentMethod == "CASH" && sale.cashTendered > 0) {
                appendLine("💵 Cash Tendered: ${KarobarFormatter.formatCurrency(sale.cashTendered, currencySymbol)}")
                appendLine("↩️ Change Due: ${KarobarFormatter.formatCurrency(sale.changeDue, currencySymbol)}")
            }
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("🙏 *آپ کی تشریف آوری کا شکریہ!*")
            appendLine("Thank you for shopping with us!")
            appendLine("⚡ _Powered by Smart Shop Pakistan_")
        }
    }

    /**
     * Sends the invoice directly to WhatsApp. If a customer phone is provided, targets that phone directly.
     */
    fun shareInvoiceViaWhatsApp(
        context: Context,
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        storeName: String,
        currencySymbol: String,
        customerPhone: String? = null,
        businessPhone: String? = null,
        businessAddress: String? = null
    ) {
        val invoiceText = buildWhatsAppInvoiceText(
            sale = sale,
            items = items,
            storeName = storeName,
            currencySymbol = currencySymbol,
            businessPhone = businessPhone,
            businessAddress = businessAddress
        )

        val cleanPhone = cleanPhoneNumber(customerPhone)
        val encodedText = try {
            URLEncoder.encode(invoiceText, "UTF-8")
        } catch (e: Exception) {
            invoiceText
        }

        try {
            if (cleanPhone.isNotBlank()) {
                val whatsappUri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedText")
                val intent = Intent(Intent.ACTION_VIEW, whatsappUri).apply {
                    setPackage("com.whatsapp")
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return
                }
                // Fallback to browser or WhatsApp Business
                val fallbackIntent = Intent(Intent.ACTION_VIEW, whatsappUri)
                context.startActivity(fallbackIntent)
            } else {
                // Share intent targeted to WhatsApp
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, invoiceText)
                    setPackage("com.whatsapp")
                }
                if (sendIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(sendIntent)
                } else {
                    val chooser = Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, invoiceText)
                        },
                        "Share Invoice"
                    )
                    context.startActivity(chooser)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not open WhatsApp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Builds a polite, professional Urdu and English Khata / Udhaar payment reminder.
     */
    fun buildUdhaarReminderText(
        customer: CustomerEntity,
        storeName: String,
        currencySymbol: String
    ): String {
        val balanceFormatted = KarobarFormatter.formatCurrency(customer.outstandingBalance, currencySymbol)
        return buildString {
            appendLine("محترم جناب *${customer.name}*،")
            appendLine("السلام علیکم!")
            appendLine("امید ہے آپ خیریت سے ہوں گے۔")
            appendLine("*${storeName}* کے کھاتے کے مطابق آپ کے ذمہ بقایا رقم:")
            appendLine("💰 *${balanceFormatted}* واجب الادا ہے۔")
            appendLine("برائے مہربانی سہولت کے مطابق جلد ادائیگی فرما دیں۔")
            appendLine("شکریہ اور نیک تمنائیں!")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("Respected *${customer.name}*,")
            appendLine("This is a polite reminder from *${storeName}* regarding your outstanding ledger balance of *${balanceFormatted}*.")
            appendLine("Please settle this at your earliest convenience. Thank you for your continued trust!")
            appendLine("⚡ _Smart Shop Khata Management_")
        }
    }

    /**
     * Sends the Khata / Udhaar reminder to the customer via WhatsApp.
     */
    fun sendUdhaarReminder(
        context: Context,
        customer: CustomerEntity,
        storeName: String,
        currencySymbol: String
    ) {
        val cleanPhone = cleanPhoneNumber(customer.phone)
        val reminderText = buildUdhaarReminderText(customer, storeName, currencySymbol)
        val encodedText = try {
            URLEncoder.encode(reminderText, "UTF-8")
        } catch (e: Exception) {
            reminderText
        }

        try {
            if (cleanPhone.isNotBlank()) {
                val whatsappUri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedText")
                val intent = Intent(Intent.ACTION_VIEW, whatsappUri).apply {
                    setPackage("com.whatsapp")
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return
                }
                // Try open intent or browser
                val fallbackIntent = Intent(Intent.ACTION_VIEW, whatsappUri)
                context.startActivity(fallbackIntent)
            } else {
                Toast.makeText(context, "Customer phone number is missing!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to SMS if WhatsApp fails
            try {
                val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("sms:${customer.phone}")
                    putExtra("sms_body", reminderText)
                }
                context.startActivity(smsIntent)
            } catch (smsError: Exception) {
                Toast.makeText(context, "Could not open messaging: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Formats and shares the Day-End Cash Closing (Z-Report) via WhatsApp.
     */
    fun shareDailyCashClosing(
        context: Context,
        storeName: String,
        dateStr: String,
        openingCash: Double,
        cashSales: Double,
        digitalSales: Double,
        udhaarRecoveries: Double,
        cashExpenses: Double,
        expectedCash: Double,
        actualCash: Double,
        variance: Double,
        notes: String,
        currencySymbol: String
    ) {
        val report = buildString {
            appendLine("📊 *DAILY CASH CLOSING / Z-REPORT*")
            appendLine("🏬 *${storeName.uppercase(Locale.US)}*")
            appendLine("📅 *Date*: $dateStr (PKT)")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("💵 *Opening Cash Float*: ${KarobarFormatter.formatCurrency(openingCash, currencySymbol)}")
            appendLine("🛍️ *Cash Sales Today*: +${KarobarFormatter.formatCurrency(cashSales, currencySymbol)}")
            appendLine("📲 *Digital Sales (EasyPaisa/Jazz)*: ${KarobarFormatter.formatCurrency(digitalSales, currencySymbol)}")
            appendLine("🤝 *Khata/Udhaar Recoveries*: +${KarobarFormatter.formatCurrency(udhaarRecoveries, currencySymbol)}")
            appendLine("💸 *Cash Expenses Paid*: -${KarobarFormatter.formatCurrency(cashExpenses, currencySymbol)}")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("💼 *EXPECTED DRAWER CASH*: *${KarobarFormatter.formatCurrency(expectedCash, currencySymbol)}*")
            appendLine("🔢 *ACTUAL PHYSICAL COUNT*: *${KarobarFormatter.formatCurrency(actualCash, currencySymbol)}*")
            val statusEmoji = if (variance >= 0) "✅" else "⚠️"
            val varianceLabel = if (variance >= 0) "Surplus/Balanced" else "SHORTAGE"
            appendLine("$statusEmoji *Variance*: *${KarobarFormatter.formatCurrency(variance, currencySymbol)}* ($varianceLabel)")
            if (notes.isNotBlank()) {
                appendLine("📝 *Shift Notes*: $notes")
            }
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("Generated via Smart Shop POS Pakistan 🇵🇰")
        }

        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, report)
                putExtra(Intent.EXTRA_SUBJECT, "Daily Cash Closing $dateStr - $storeName")
                setPackage("com.whatsapp")
            }
            if (sendIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(sendIntent)
            } else {
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, report)
                    },
                    "Share Daily Z-Report"
                )
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not share report: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
