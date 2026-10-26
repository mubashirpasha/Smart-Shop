package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.localization.KarobarFormatter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object InvoicePdfGenerator {

    fun generateInvoicePdf(
        context: Context,
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        storeName: String,
        currencySymbol: String,
        invoiceFooter: String? = null,
        businessPhone: String? = null,
        businessAddress: String? = null,
        businessNtn: String? = null,
        invoiceTemplate: String = "STANDARD",
        showTaxNumber: Boolean = true,
        showCustomerPhone: Boolean = true,
        showShopAddress: Boolean = true
    ): File? {
        val document = PdfDocument()

        try {
            // A4 page dimensions: 595 x 842 points
            val pageWidth = 595
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint().apply {
                isAntiAlias = true
            }

            val margin = 40f
            var yPos = 50f

            // Header Background Accent Bar
            paint.color = Color.rgb(15, 23, 42) // Dark Navy
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 110f, paint)

            paint.color = Color.rgb(16, 185, 129) // Emerald Accent
            canvas.drawRect(0f, 106f, pageWidth.toFloat(), 110f, paint)

            // Store Name & Logo Text
            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 20f
            canvas.drawText(storeName.uppercase(Locale.US), margin, 50f, paint)

            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(203, 213, 225)
            val addressLine = if (showShopAddress && !businessAddress.isNullOrBlank()) businessAddress else "KAROBARFLOW • سمارٹ کاروباری نظام • PAKISTAN"
            canvas.drawText(addressLine, margin, 68f, paint)

            val phoneNtnLine = buildString {
                if (showShopAddress && !businessPhone.isNullOrBlank()) append("Tel: $businessPhone")
                if (showTaxNumber && !businessNtn.isNullOrBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append("NTN: $businessNtn")
                }
            }
            if (phoneNtnLine.isNotBlank()) {
                paint.textSize = 9f
                paint.color = Color.rgb(148, 163, 184)
                canvas.drawText(phoneNtnLine, margin, 84f, paint)
            }

            // Invoice Title on Top Right
            paint.textAlign = Paint.Align.RIGHT
            paint.color = Color.WHITE
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val invoiceHeaderTitle = when (invoiceTemplate) {
                "THERMAL" -> "POS RECEIPT"
                "DETAILED" -> "DETAILED TAX INVOICE"
                else -> "TAX INVOICE"
            }
            canvas.drawText(invoiceHeaderTitle, pageWidth - margin, 52f, paint)

            paint.textSize = 11f
            paint.color = Color.rgb(56, 189, 248) // Cyan
            canvas.drawText(sale.invoiceNumber, pageWidth - margin, 72f, paint)
            paint.textAlign = Paint.Align.LEFT

            yPos = 135f

            // Meta Info Section (Date in DD-MM-YYYY Asia/Karachi, Payment, Customer)
            val dateStr = KarobarFormatter.formatDateTime(sale.timestamp)

            paint.textSize = 9.5f
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText("DATE & TIME (PKT)", margin, yPos, paint)
            canvas.drawText("PAYMENT METHOD", margin + 190f, yPos, paint)
            canvas.drawText("CUSTOMER", pageWidth - margin - 140f, yPos, paint)

            yPos += 16f
            paint.textSize = 10.5f
            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(dateStr, margin, yPos, paint)
            canvas.drawText(sale.paymentMethod, margin + 190f, yPos, paint)
            canvas.drawText(sale.customerName.ifBlank { "Walk-in Customer" }, pageWidth - margin - 140f, yPos, paint)

            if (showCustomerPhone && sale.customerNote.contains("Phone:")) {
                yPos += 14f
                val phoneIndex = sale.customerNote.indexOf("Phone:")
                val customerPhoneStr = sale.customerNote.substring(phoneIndex)
                paint.textSize = 9.5f
                paint.color = Color.rgb(71, 85, 105)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(customerPhoneStr, pageWidth - margin - 140f, yPos, paint)
            }

            yPos += 26f

            // Table Header Bar
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 26f, paint)

            paint.color = Color.rgb(71, 85, 105)
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("ITEM DESCRIPTION", margin + 10f, yPos + 17f, paint)
            canvas.drawText("QTY", margin + 300f, yPos + 17f, paint)
            canvas.drawText("PRICE", margin + 360f, yPos + 17f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("TOTAL", pageWidth - margin - 10f, yPos + 17f, paint)
            paint.textAlign = Paint.Align.LEFT

            yPos += 36f

            // Line Items
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f

            items.forEachIndexed { index, item ->
                if (index % 2 == 1) {
                    paint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(margin, yPos - 12f, pageWidth - margin, yPos + 16f, paint)
                }

                paint.color = Color.rgb(30, 41, 59)
                canvas.drawText(item.productName, margin + 10f, yPos + 2f, paint)
                canvas.drawText("${item.quantity}x", margin + 300f, yPos + 2f, paint)
                canvas.drawText(KarobarFormatter.formatCurrency(item.unitPrice, currencySymbol), margin + 360f, yPos + 2f, paint)

                paint.textAlign = Paint.Align.RIGHT
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(KarobarFormatter.formatCurrency(item.subtotal, currencySymbol), pageWidth - margin - 10f, yPos + 2f, paint)
                paint.textAlign = Paint.Align.LEFT
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                yPos += 24f
            }

            yPos += 15f

            // Divider Line
            paint.color = Color.rgb(203, 213, 225)
            paint.strokeWidth = 1f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paint)

            yPos += 20f

            // Summary Totals
            val summaryX = pageWidth - margin - 230f
            val valueX = pageWidth - margin - 10f

            fun drawSummaryRow(label: String, value: String, isBold: Boolean = false, isAccent: Boolean = false) {
                paint.typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = if (isAccent) Color.rgb(16, 185, 129) else Color.rgb(71, 85, 105)
                paint.textSize = if (isBold) 12.5f else 10.5f
                canvas.drawText(label, summaryX, yPos, paint)

                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(value, valueX, yPos, paint)
                paint.textAlign = Paint.Align.LEFT

                yPos += 18f
            }

            drawSummaryRow("Subtotal", KarobarFormatter.formatCurrency(sale.subtotal, currencySymbol))

            if (sale.discountAmount > 0) {
                drawSummaryRow("Discount (${String.format(Locale.US, "%.0f", sale.discountPercent)}%)", "-${KarobarFormatter.formatCurrency(sale.discountAmount, currencySymbol)}")
            }

            if (sale.taxAmount > 0) {
                drawSummaryRow("Sales Tax (${String.format(Locale.US, "%.1f", sale.taxRate)}%)", "+${KarobarFormatter.formatCurrency(sale.taxAmount, currencySymbol)}")
            }

            yPos += 4f
            paint.color = Color.rgb(16, 185, 129)
            paint.strokeWidth = 1.5f
            canvas.drawLine(summaryX, yPos, valueX, yPos, paint)
            yPos += 18f

            drawSummaryRow("GRAND TOTAL", KarobarFormatter.formatCurrency(sale.totalAmount, currencySymbol), isBold = true, isAccent = true)

            if (sale.paymentMethod == "CASH" && sale.cashTendered > 0) {
                yPos += 6f
                drawSummaryRow("Cash Received", KarobarFormatter.formatCurrency(sale.cashTendered, currencySymbol))
                drawSummaryRow("Change Due", KarobarFormatter.formatCurrency(sale.changeDue, currencySymbol))
            }

            // Footer Section
            val footerY = pageHeight - 60f
            paint.color = Color.rgb(226, 232, 240)
            canvas.drawLine(margin, footerY, pageWidth - margin, footerY, paint)

            paint.color = Color.rgb(148, 163, 184)
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val footerText = invoiceFooter?.takeIf { it.isNotBlank() }
                ?: "Thank you for your business! Computerized tax invoice generated by KarobarFlow."
            canvas.drawText(footerText, margin, footerY + 18f, paint)
            paint.textAlign = Paint.Align.RIGHT
            val ntnText = if (!businessNtn.isNullOrBlank()) "NTN: $businessNtn • پاکستان" else "Verified • پاکستان"
            canvas.drawText(ntnText, pageWidth - margin, footerY + 18f, paint)
            paint.textAlign = Paint.Align.LEFT

            document.finishPage(page)

            // Save to internal cache dir
            val invoicesDir = File(context.cacheDir, "invoices").apply { mkdirs() }
            val pdfFile = File(invoicesDir, "${sale.invoiceNumber}.pdf")
            FileOutputStream(pdfFile).use { out ->
                document.writeTo(out)
            }
            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            document.close()
        }
    }

    fun shareInvoicePdf(
        context: Context,
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        storeName: String,
        currencySymbol: String,
        invoiceFooter: String? = null,
        businessPhone: String? = null,
        businessAddress: String? = null,
        businessNtn: String? = null,
        invoiceTemplate: String = "STANDARD",
        showTaxNumber: Boolean = true,
        showCustomerPhone: Boolean = true,
        showShopAddress: Boolean = true
    ) {
        val pdfFile = generateInvoicePdf(
            context = context,
            sale = sale,
            items = items,
            storeName = storeName,
            currencySymbol = currencySymbol,
            invoiceFooter = invoiceFooter,
            businessPhone = businessPhone,
            businessAddress = businessAddress,
            businessNtn = businessNtn,
            invoiceTemplate = invoiceTemplate,
            showTaxNumber = showTaxNumber,
            showCustomerPhone = showCustomerPhone,
            showShopAddress = showShopAddress
        )
        if (pdfFile != null && pdfFile.exists()) {
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Tax Invoice ${sale.invoiceNumber} - $storeName")
                    putExtra(Intent.EXTRA_TEXT, "Attached is tax invoice ${sale.invoiceNumber} from $storeName via KarobarFlow.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
            } catch (e: Exception) {
                shareInvoiceAsText(context, sale, items, storeName, currencySymbol, invoiceFooter, businessPhone, businessAddress, businessNtn, invoiceTemplate, showTaxNumber, showCustomerPhone, showShopAddress)
            }
        } else {
            shareInvoiceAsText(context, sale, items, storeName, currencySymbol, invoiceFooter, businessPhone, businessAddress, businessNtn, invoiceTemplate, showTaxNumber, showCustomerPhone, showShopAddress)
        }
    }

    fun shareInvoiceAsText(
        context: Context,
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        storeName: String,
        currencySymbol: String,
        invoiceFooter: String? = null,
        businessPhone: String? = null,
        businessAddress: String? = null,
        businessNtn: String? = null,
        invoiceTemplate: String = "STANDARD",
        showTaxNumber: Boolean = true,
        showCustomerPhone: Boolean = true,
        showShopAddress: Boolean = true
    ) {
        val dateStr = KarobarFormatter.formatDateTime(sale.timestamp)

        val receiptText = buildString {
            when (invoiceTemplate) {
                "THERMAL" -> {
                    appendLine("--------------------------------")
                    appendLine("       ${storeName.uppercase(Locale.US)}")
                    if (showShopAddress && !businessAddress.isNullOrBlank()) {
                        appendLine("   $businessAddress")
                    }
                    if (showShopAddress && !businessPhone.isNullOrBlank()) {
                        appendLine("   Tel: $businessPhone")
                    }
                    if (showTaxNumber && !businessNtn.isNullOrBlank()) {
                        appendLine("   NTN: $businessNtn")
                    }
                    appendLine("--------------------------------")
                    appendLine("RCVD: ${sale.invoiceNumber} | $dateStr")
                    appendLine("MODE: ${sale.paymentMethod}")
                    if (sale.customerName.isNotBlank()) {
                        appendLine("CUST: ${sale.customerName}")
                    }
                    appendLine("--------------------------------")
                    items.forEach { item ->
                        appendLine("${item.quantity}x ${item.productName}")
                        appendLine("   @ ${KarobarFormatter.formatCurrency(item.unitPrice, currencySymbol)} = ${KarobarFormatter.formatCurrency(item.subtotal, currencySymbol)}")
                    }
                    appendLine("--------------------------------")
                    appendLine("SUBTOTAL : ${KarobarFormatter.formatCurrency(sale.subtotal, currencySymbol)}")
                    if (sale.discountAmount > 0) {
                        appendLine("DISCOUNT : -${KarobarFormatter.formatCurrency(sale.discountAmount, currencySymbol)} (${String.format(Locale.US, "%.0f", sale.discountPercent)}%)")
                    }
                    if (sale.taxAmount > 0) {
                        appendLine("TAX      : +${KarobarFormatter.formatCurrency(sale.taxAmount, currencySymbol)} (${String.format(Locale.US, "%.1f", sale.taxRate)}%)")
                    }
                    appendLine("TOTAL    : ${KarobarFormatter.formatCurrency(sale.totalAmount, currencySymbol)}")
                    if (sale.paymentMethod == "CASH" && sale.cashTendered > 0) {
                        appendLine("CASH PAID: ${KarobarFormatter.formatCurrency(sale.cashTendered, currencySymbol)}")
                        appendLine("CHANGE   : ${KarobarFormatter.formatCurrency(sale.changeDue, currencySymbol)}")
                    }
                    appendLine("--------------------------------")
                    val footerText = invoiceFooter?.takeIf { it.isNotBlank() }
                        ?: "Thank you for your visit!"
                    appendLine(footerText)
                    appendLine("Powered by KarobarFlow Pakistan")
                }
                "DETAILED" -> {
                    appendLine("==========================================")
                    appendLine("         DETAILED TAX SALES INVOICE       ")
                    appendLine("         ${storeName.uppercase(Locale.US)}")
                    if (showShopAddress && !businessAddress.isNullOrBlank()) {
                        appendLine("Address: $businessAddress")
                    }
                    if (showShopAddress && !businessPhone.isNullOrBlank()) {
                        appendLine("Contact: $businessPhone")
                    }
                    if (showTaxNumber && !businessNtn.isNullOrBlank()) {
                        appendLine("NTN Registration: $businessNtn")
                    }
                    appendLine("==========================================")
                    appendLine("Invoice No : ${sale.invoiceNumber}")
                    appendLine("Date & Time: $dateStr (PKT)")
                    appendLine("Payment    : ${sale.paymentMethod}")
                    if (sale.customerName.isNotBlank()) {
                        appendLine("Billed To  : ${sale.customerName}")
                    }
                    if (showCustomerPhone && sale.customerNote.contains("Phone:")) {
                        val pIdx = sale.customerNote.indexOf("Phone:")
                        appendLine("Phone      : ${sale.customerNote.substring(pIdx)}")
                    }
                    if (sale.customerNote.isNotBlank()) {
                        appendLine("Memo / Note: ${sale.customerNote}")
                    }
                    appendLine("------------------------------------------")
                    appendLine("ITEMIZED BREAKDOWN:")
                    items.forEachIndexed { i, item ->
                        appendLine("${i + 1}. ${item.productName}")
                        appendLine("   ${item.quantity} units x ${KarobarFormatter.formatCurrency(item.unitPrice, currencySymbol)} = ${KarobarFormatter.formatCurrency(item.subtotal, currencySymbol)}")
                    }
                    appendLine("------------------------------------------")
                    appendLine("Gross Subtotal : ${KarobarFormatter.formatCurrency(sale.subtotal, currencySymbol)}")
                    if (sale.discountAmount > 0) {
                        appendLine("Discount Allowed: -${KarobarFormatter.formatCurrency(sale.discountAmount, currencySymbol)} (${String.format(Locale.US, "%.0f", sale.discountPercent)}%)")
                    }
                    if (sale.taxAmount > 0) {
                        appendLine("Sales Tax (${String.format(Locale.US, "%.1f", sale.taxRate)}%): +${KarobarFormatter.formatCurrency(sale.taxAmount, currencySymbol)}")
                    }
                    appendLine("NET GRAND TOTAL: ${KarobarFormatter.formatCurrency(sale.totalAmount, currencySymbol)}")
                    if (sale.paymentMethod == "CASH" && sale.cashTendered > 0) {
                        appendLine("Amount Tendered: ${KarobarFormatter.formatCurrency(sale.cashTendered, currencySymbol)}")
                        appendLine("Balance Change : ${KarobarFormatter.formatCurrency(sale.changeDue, currencySymbol)}")
                    }
                    appendLine("==========================================")
                    val footerText = invoiceFooter?.takeIf { it.isNotBlank() }
                        ?: "Thank you for your business! Computerized tax invoice generated by KarobarFlow."
                    appendLine(footerText)
                    appendLine("KarobarFlow • سمارٹ ریٹیل مینجمنٹ پاکستان")
                }
                else -> {
                    appendLine("==========================================")
                    appendLine("       $storeName TAX INVOICE             ")
                    appendLine("       کاروبار فلو - KarobarFlow          ")
                    if (showShopAddress && !businessAddress.isNullOrBlank()) {
                        appendLine("       $businessAddress")
                    }
                    if (showShopAddress && !businessPhone.isNullOrBlank()) {
                        appendLine("       Tel: $businessPhone")
                    }
                    if (showTaxNumber && !businessNtn.isNullOrBlank()) {
                        appendLine("       NTN: $businessNtn")
                    }
                    appendLine("==========================================")
                    appendLine("Invoice No : ${sale.invoiceNumber}")
                    appendLine("Date (PKT) : $dateStr")
                    appendLine("Payment    : ${sale.paymentMethod}")
                    if (sale.customerName.isNotBlank()) {
                        appendLine("Customer   : ${sale.customerName}")
                    }
                    appendLine("------------------------------------------")
                    appendLine("ITEMS:")
                    items.forEach { item ->
                        appendLine(
                            "  ${item.quantity}x ${item.productName} @ ${KarobarFormatter.formatCurrency(item.unitPrice, currencySymbol)} = ${KarobarFormatter.formatCurrency(item.subtotal, currencySymbol)}"
                        )
                    }
                    appendLine("------------------------------------------")
                    appendLine("Subtotal   : ${KarobarFormatter.formatCurrency(sale.subtotal, currencySymbol)}")
                    if (sale.discountAmount > 0) {
                        appendLine("Discount   : -${KarobarFormatter.formatCurrency(sale.discountAmount, currencySymbol)} (${String.format(Locale.US, "%.0f", sale.discountPercent)}%)")
                    }
                    if (sale.taxAmount > 0) {
                        appendLine("Tax        : +${KarobarFormatter.formatCurrency(sale.taxAmount, currencySymbol)} (${String.format(Locale.US, "%.1f", sale.taxRate)}%)")
                    }
                    appendLine("TOTAL DUE  : ${KarobarFormatter.formatCurrency(sale.totalAmount, currencySymbol)}")
                    if (sale.paymentMethod == "CASH" && sale.cashTendered > 0) {
                        appendLine("Cash Paid  : ${KarobarFormatter.formatCurrency(sale.cashTendered, currencySymbol)}")
                        appendLine("Change Due : ${KarobarFormatter.formatCurrency(sale.changeDue, currencySymbol)}")
                    }
                    appendLine("==========================================")
                    val footerText = invoiceFooter?.takeIf { it.isNotBlank() }
                        ?: "Thank you for your business! Computerized tax invoice generated by KarobarFlow."
                    appendLine(footerText)
                    appendLine("Powered by KarobarFlow Pakistan")
                }
            }
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, receiptText)
            putExtra(Intent.EXTRA_SUBJECT, "Invoice ${sale.invoiceNumber} - $storeName")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Receipt")
        context.startActivity(shareIntent)
    }
}
