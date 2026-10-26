package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.localization.KarobarFormatter
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.util.Locale

object ThermalReceiptPrinter {

    /**
     * Prints a 58mm / 80mm POS thermal receipt using Android's PrintManager framework.
     * This connects automatically to any paired Bluetooth thermal printer, USB receipt printer,
     * or network printer via the Android Print Spooler.
     */
    fun printReceipt(
        context: Context,
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        storeName: String,
        currencySymbol: String,
        businessPhone: String? = null,
        businessAddress: String? = null,
        businessNtn: String? = null,
        invoiceFooter: String? = null
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "Print service is not available on this device", Toast.LENGTH_SHORT).show()
            return
        }

        val jobName = "Receipt_${sale.invoiceNumber}"
        val printAdapter = object : PrintDocumentAdapter() {
            private var pdfDocument: PdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()

                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }

                try {
                    // Standard 58mm / 80mm thermal receipt dimensions in points (72 points = 1 inch)
                    // 58mm = ~200 points wide, 80mm = ~280 points wide. Using 226 points (58mm POS standard)
                    val receiptWidth = 226
                    // Dynamic height based on number of items
                    val receiptHeight = 380 + (items.size * 22)

                    pdfDocument = PdfDocument()
                    val pageInfo = PdfDocument.PageInfo.Builder(receiptWidth, receiptHeight, 1).create()
                    val page = pdfDocument!!.startPage(pageInfo)
                    val canvas: Canvas = page.canvas

                    val paint = Paint().apply {
                        isAntiAlias = true
                    }

                    var y = 24f
                    val leftMargin = 10f
                    val rightMargin = receiptWidth - 10f
                    val centerX = receiptWidth / 2f

                    // Header Store Name
                    paint.textAlign = Paint.Align.CENTER
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 13f
                    paint.color = Color.BLACK
                    canvas.drawText(storeName.uppercase(Locale.US), centerX, y, paint)

                    if (!businessAddress.isNullOrBlank()) {
                        y += 13f
                        paint.textSize = 7.5f
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        canvas.drawText(businessAddress, centerX, y, paint)
                    }

                    if (!businessPhone.isNullOrBlank()) {
                        y += 11f
                        paint.textSize = 7.5f
                        canvas.drawText("Tel: $businessPhone", centerX, y, paint)
                    }

                    if (!businessNtn.isNullOrBlank()) {
                        y += 11f
                        paint.textSize = 7.5f
                        canvas.drawText("NTN: $businessNtn", centerX, y, paint)
                    }

                    // Dashed separator
                    y += 12f
                    paint.textSize = 8f
                    paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                    canvas.drawText("--------------------------------", centerX, y, paint)

                    // Meta Info
                    y += 13f
                    paint.textAlign = Paint.Align.LEFT
                    paint.textSize = 7.5f
                    canvas.drawText("INV: ${sale.invoiceNumber}", leftMargin, y, paint)

                    val dateStr = KarobarFormatter.formatDateTime(sale.timestamp)
                    y += 11f
                    canvas.drawText("DATE: $dateStr", leftMargin, y, paint)

                    y += 11f
                    canvas.drawText("PAY: ${sale.paymentMethod}", leftMargin, y, paint)
                    if (sale.customerName.isNotBlank()) {
                        y += 11f
                        canvas.drawText("CUST: ${sale.customerName}", leftMargin, y, paint)
                    }

                    // Table Separator
                    y += 10f
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("--------------------------------", centerX, y, paint)

                    // Line Items
                    y += 13f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    items.forEach { item ->
                        paint.textAlign = Paint.Align.LEFT
                        canvas.drawText("${item.quantity}x ${item.productName.take(18)}", leftMargin, y, paint)

                        paint.textAlign = Paint.Align.RIGHT
                        canvas.drawText(KarobarFormatter.formatCurrency(item.subtotal, currencySymbol), rightMargin, y, paint)
                        y += 14f
                    }

                    // Total Separator
                    y += 2f
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("--------------------------------", centerX, y, paint)

                    // Totals
                    y += 13f
                    paint.textAlign = Paint.Align.LEFT
                    canvas.drawText("Subtotal:", leftMargin, y, paint)
                    paint.textAlign = Paint.Align.RIGHT
                    canvas.drawText(KarobarFormatter.formatCurrency(sale.subtotal, currencySymbol), rightMargin, y, paint)

                    if (sale.discountAmount > 0) {
                        y += 12f
                        paint.textAlign = Paint.Align.LEFT
                        canvas.drawText("Discount:", leftMargin, y, paint)
                        paint.textAlign = Paint.Align.RIGHT
                        canvas.drawText("-${KarobarFormatter.formatCurrency(sale.discountAmount, currencySymbol)}", rightMargin, y, paint)
                    }

                    if (sale.taxAmount > 0) {
                        y += 12f
                        paint.textAlign = Paint.Align.LEFT
                        canvas.drawText("Sales Tax:", leftMargin, y, paint)
                        paint.textAlign = Paint.Align.RIGHT
                        canvas.drawText("+${KarobarFormatter.formatCurrency(sale.taxAmount, currencySymbol)}", rightMargin, y, paint)
                    }

                    y += 14f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 9.5f
                    paint.textAlign = Paint.Align.LEFT
                    canvas.drawText("TOTAL DUE:", leftMargin, y, paint)
                    paint.textAlign = Paint.Align.RIGHT
                    canvas.drawText(KarobarFormatter.formatCurrency(sale.totalAmount, currencySymbol), rightMargin, y, paint)

                    if (sale.paymentMethod == "CASH" && sale.cashTendered > 0) {
                        y += 13f
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        paint.textSize = 7.5f
                        paint.textAlign = Paint.Align.LEFT
                        canvas.drawText("Cash Paid:", leftMargin, y, paint)
                        paint.textAlign = Paint.Align.RIGHT
                        canvas.drawText(KarobarFormatter.formatCurrency(sale.cashTendered, currencySymbol), rightMargin, y, paint)

                        y += 11f
                        paint.textAlign = Paint.Align.LEFT
                        canvas.drawText("Change Due:", leftMargin, y, paint)
                        paint.textAlign = Paint.Align.RIGHT
                        canvas.drawText(KarobarFormatter.formatCurrency(sale.changeDue, currencySymbol), rightMargin, y, paint)
                    }

                    // Footer
                    y += 14f
                    paint.textAlign = Paint.Align.CENTER
                    paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                    canvas.drawText("--------------------------------", centerX, y, paint)

                    y += 12f
                    paint.textSize = 7f
                    val footer = invoiceFooter?.takeIf { it.isNotBlank() } ?: "Thank you! Please visit again."
                    canvas.drawText(footer, centerX, y, paint)

                    y += 11f
                    canvas.drawText("Smart Shop POS Pakistan", centerX, y, paint)

                    pdfDocument!!.finishPage(page)

                    destination?.let { desc ->
                        FileOutputStream(desc.fileDescriptor).use { out ->
                            pdfDocument!!.writeTo(out)
                        }
                    }

                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback?.onWriteFailed(e.message)
                } finally {
                    pdfDocument?.close()
                }
            }
        }

        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A7)
            .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
            .build()

        printManager.print(jobName, printAdapter, printAttributes)
    }

    /**
     * Generates standard ESC/POS command bytes for 58mm thermal printers.
     * Ready for direct Bluetooth SPP Socket output if a paired Bluetooth printer is used.
     */
    fun buildEscPosBytes(
        storeName: String,
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        currencySymbol: String
    ): ByteArray {
        val out = ByteArrayOutputStream()
        // ESC @ -> Initialize printer
        out.write(byteArrayOf(0x1B, 0x40))

        // ESC a 1 -> Center align
        out.write(byteArrayOf(0x1B, 0x61, 0x01))
        // ESC E 1 -> Bold on
        out.write(byteArrayOf(0x1B, 0x45, 0x01))
        // GS ! 0x11 -> Double height + double width for store name
        out.write(byteArrayOf(0x1D, 0x21, 0x11))
        out.write("${storeName}\n".toByteArray(Charsets.US_ASCII))

        // Reset text format
        out.write(byteArrayOf(0x1D, 0x21, 0x00))
        out.write(byteArrayOf(0x1B, 0x45, 0x00))

        out.write("--------------------------------\n".toByteArray(Charsets.US_ASCII))
        // ESC a 0 -> Left align
        out.write(byteArrayOf(0x1B, 0x61, 0x00))
        out.write("Invoice: ${sale.invoiceNumber}\n".toByteArray(Charsets.US_ASCII))
        out.write("Date: ${KarobarFormatter.formatDateTime(sale.timestamp)}\n".toByteArray(Charsets.US_ASCII))
        out.write("Mode: ${sale.paymentMethod}\n".toByteArray(Charsets.US_ASCII))
        out.write("--------------------------------\n".toByteArray(Charsets.US_ASCII))

        items.forEach { item ->
            val line = "${item.quantity}x ${item.productName.take(16)}: ${item.subtotal.toInt()}\n"
            out.write(line.toByteArray(Charsets.US_ASCII))
        }

        out.write("--------------------------------\n".toByteArray(Charsets.US_ASCII))
        out.write(byteArrayOf(0x1B, 0x45, 0x01))
        out.write("TOTAL: $currencySymbol ${sale.totalAmount.toInt()}\n".toByteArray(Charsets.US_ASCII))
        out.write(byteArrayOf(0x1B, 0x45, 0x00))

        out.write("--------------------------------\n".toByteArray(Charsets.US_ASCII))
        // Center
        out.write(byteArrayOf(0x1B, 0x61, 0x01))
        out.write("Thank you for your visit!\n\n\n\n".toByteArray(Charsets.US_ASCII))
        // GS V 0 -> Cut paper
        out.write(byteArrayOf(0x1D, 0x56, 0x00))

        return out.toByteArray()
    }
}
