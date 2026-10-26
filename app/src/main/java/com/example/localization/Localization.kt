package com.example.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    URDU("ur", "Urdu", "اردو")
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }

/**
 * Decimal-safe arithmetic for all financial operations in KarobarFlow.
 * Prevents floating point inaccuracy in currency calculations, discounts, taxes, and ledgers.
 */
object KarobarMath {
    fun round(value: Double): Double =
        BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toDouble()

    fun add(a: Double, b: Double): Double =
        BigDecimal.valueOf(a).add(BigDecimal.valueOf(b)).setScale(2, RoundingMode.HALF_UP).toDouble()

    fun subtract(a: Double, b: Double): Double =
        BigDecimal.valueOf(a).subtract(BigDecimal.valueOf(b)).setScale(2, RoundingMode.HALF_UP).toDouble()

    fun multiply(a: Double, b: Double): Double =
        BigDecimal.valueOf(a).multiply(BigDecimal.valueOf(b)).setScale(2, RoundingMode.HALF_UP).toDouble()

    fun divide(a: Double, b: Double): Double {
        if (b == 0.0) return 0.0
        return BigDecimal.valueOf(a).divide(BigDecimal.valueOf(b), 4, RoundingMode.HALF_UP).toDouble()
    }

    fun calculateDiscount(subtotal: Double, percent: Double): Double =
        BigDecimal.valueOf(subtotal)
            .multiply(BigDecimal.valueOf(percent))
            .divide(BigDecimal.valueOf(100.0), 2, RoundingMode.HALF_UP)
            .toDouble()

    fun calculateTax(amountAfterDiscount: Double, taxRate: Double): Double =
        BigDecimal.valueOf(amountAfterDiscount)
            .multiply(BigDecimal.valueOf(taxRate))
            .divide(BigDecimal.valueOf(100.0), 2, RoundingMode.HALF_UP)
            .toDouble()

    fun calculateTotal(subtotal: Double, discountAmount: Double, taxAmount: Double): Double {
        val s = BigDecimal.valueOf(subtotal)
        val d = BigDecimal.valueOf(discountAmount)
        val t = BigDecimal.valueOf(taxAmount)
        return s.subtract(d).add(t).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    fun calculateProfit(sellingPrice: Double, costPrice: Double, quantity: Int): Double {
        val sp = BigDecimal.valueOf(sellingPrice)
        val cp = BigDecimal.valueOf(costPrice)
        val qty = BigDecimal.valueOf(quantity.toLong())
        return sp.subtract(cp).multiply(qty).setScale(2, RoundingMode.HALF_UP).toDouble()
    }
}

data class PhoneValidationResult(
    val isValid: Boolean,
    val originalInput: String,
    val normalizedE164: String,    // e.g., "+923001234567"
    val normalizedLocal: String,   // e.g., "03001234567"
    val formattedDisplay: String,  // e.g., "0300-1234567" or "+92 300 1234567"
    val errorMessageEn: String? = null,
    val errorMessageUr: String? = null
)

object KarobarFormatter {
    val karachiTimeZone: TimeZone = TimeZone.getTimeZone("Asia/Karachi")

    /**
     * Pakistani number and currency formatting:
     * Examples:
     * - Rs. 1,000
     * - Rs. 25,000
     * - Rs. 150,000
     * If decimals are non-zero, formats with two decimal places (e.g. Rs. 1,000.50).
     */
    fun formatCurrency(amount: Double, currencySymbol: String = "Rs."): String {
        val bd = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }
        val df = if (bd.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            DecimalFormat("#,##0", symbols)
        } else {
            DecimalFormat("#,##0.00", symbols)
        }
        val cleanSymbol = currencySymbol.trim()
        val symbolFormatted = when {
            cleanSymbol.isBlank() -> ""
            cleanSymbol.equals("Rs", ignoreCase = true) -> "Rs."
            else -> cleanSymbol
        }
        val formattedNumber = df.format(bd)
        return if (symbolFormatted.isBlank()) formattedNumber else "$symbolFormatted $formattedNumber"
    }

    /**
     * Default date format: DD-MM-YYYY in Asia/Karachi timezone
     */
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US).apply {
            timeZone = karachiTimeZone
        }
        return sdf.format(Date(timestamp))
    }

    /**
     * Default date and time format in Asia/Karachi timezone
     */
    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy • hh:mm a", Locale.US).apply {
            timeZone = karachiTimeZone
        }
        return sdf.format(Date(timestamp))
    }

    /**
     * Validates Pakistani phone number formats:
     * - 03XXXXXXXXX (11 digits)
     * - +923XXXXXXXXX (13 chars)
     * Also handles inputs with spaces/hyphens (e.g. 0300 1234567 or +92 300 1234567).
     */
    fun validatePakistaniPhone(phone: String): PhoneValidationResult {
        val trimmed = phone.trim()
        val cleaned = trimmed.replace(" ", "").replace("-", "").replace("(", "").replace(")", "")

        val isLocalFormat = cleaned.matches(Regex("^03\\d{9}$"))
        val isIntlFormat = cleaned.matches(Regex("^\\+923\\d{9}$"))
        val isZeroIntlFormat = cleaned.matches(Regex("^00923\\d{9}$"))
        val isWithoutPlusFormat = cleaned.matches(Regex("^923\\d{9}$"))

        if (isLocalFormat) {
            val local = cleaned
            val e164 = "+92${cleaned.substring(1)}"
            val display = "${local.substring(0, 4)}-${local.substring(4)}"
            return PhoneValidationResult(
                isValid = true,
                originalInput = trimmed,
                normalizedE164 = e164,
                normalizedLocal = local,
                formattedDisplay = display
            )
        } else if (isIntlFormat) {
            val e164 = cleaned
            val local = "0${cleaned.substring(3)}"
            val display = "${e164.substring(0, 3)} ${e164.substring(3, 6)} ${e164.substring(6)}"
            return PhoneValidationResult(
                isValid = true,
                originalInput = trimmed,
                normalizedE164 = e164,
                normalizedLocal = local,
                formattedDisplay = display
            )
        } else if (isWithoutPlusFormat) {
            val e164 = "+$cleaned"
            val local = "0${cleaned.substring(2)}"
            val display = "${e164.substring(0, 3)} ${e164.substring(3, 6)} ${e164.substring(6)}"
            return PhoneValidationResult(
                isValid = true,
                originalInput = trimmed,
                normalizedE164 = e164,
                normalizedLocal = local,
                formattedDisplay = display
            )
        } else if (isZeroIntlFormat) {
            val e164 = "+${cleaned.substring(2)}"
            val local = "0${cleaned.substring(4)}"
            val display = "${e164.substring(0, 3)} ${e164.substring(3, 6)} ${e164.substring(6)}"
            return PhoneValidationResult(
                isValid = true,
                originalInput = trimmed,
                normalizedE164 = e164,
                normalizedLocal = local,
                formattedDisplay = display
            )
        }

        return PhoneValidationResult(
            isValid = false,
            originalInput = trimmed,
            normalizedE164 = trimmed,
            normalizedLocal = trimmed,
            formattedDisplay = trimmed,
            errorMessageEn = "Invalid phone format. Please enter a valid Pakistani number (e.g., 03001234567 or +923001234567).",
            errorMessageUr = "غلط فون فارمیٹ۔ درست پاکستانی موبائل نمبر درج کریں (مثلاً 03001234567 یا 923001234567+)"
        )
    }

    fun isValidPakistaniPhone(phone: String): Boolean =
        validatePakistaniPhone(phone).isValid

    fun normalizePakistaniPhone(phone: String): String =
        validatePakistaniPhone(phone).normalizedE164
}

/**
 * Pakistani Business Categories as required by brief:
 * - General Store
 * - Grocery Store
 * - Garments
 * - Electronics
 * - Cosmetics
 * - Pharmacy
 * - Mobile Accessories
 * - Hardware
 * - Stationery
 * - Restaurant
 * - Other
 * Plus customizable categories support.
 */
val BusinessCategoriesEn = listOf(
    "General Store",
    "Grocery Store",
    "Garments",
    "Electronics",
    "Cosmetics",
    "Pharmacy",
    "Mobile Accessories",
    "Hardware",
    "Stationery",
    "Restaurant",
    "Other"
)

val BusinessCategoriesUr = listOf(
    "جنرل اسٹور",
    "کرانہ اسٹور",
    "گارمنٹس / کپڑے",
    "الیکٹرانکس",
    "کاسمیٹکس",
    "فارمیسی / میڈیکل اسٹور",
    "موبائل اسیسریز",
    "ہارڈویئر",
    "اسٹیشنری",
    "ریسٹورنٹ / ہوٹل",
    "دیگر"
)

/**
 * 6 Selectable Pakistani Payment Record Types (Recording options only):
 * - Cash
 * - Bank Transfer
 * - JazzCash
 * - Easypaisa
 * - Credit / Udhaar
 * - Other
 */
data class PaymentMethodOption(
    val code: String,
    val nameEn: String,
    val nameUr: String,
    val colorHex: Long
)

val KarobarPaymentMethods = listOf(
    PaymentMethodOption("CASH", "Cash", "نقد (کیش)", 0xFF10B981),
    PaymentMethodOption("BANK", "Bank Transfer", "بینک ٹرانسفر", 0xFF6366F1),
    PaymentMethodOption("JAZZCASH", "JazzCash", "جاز کیش", 0xFFFF5722),
    PaymentMethodOption("EASYPAISA", "Easypaisa", "ایزی پیسہ", 0xFF00C853),
    PaymentMethodOption("CREDIT", "Credit / Udhaar", "ادھار کھاتہ", 0xFF06B6D4),
    PaymentMethodOption("OTHER", "Other", "دیگر", 0xFFA855F7)
)

val PaymentMethodsEn = KarobarPaymentMethods.map { it.nameEn }
val PaymentMethodsUr = KarobarPaymentMethods.map { it.nameUr }

fun getCategoryLabel(category: String, isUrdu: Boolean): String {
    val index = BusinessCategoriesEn.indexOfFirst { it.equals(category, ignoreCase = true) }
    return if (isUrdu && index != -1 && index < BusinessCategoriesUr.size) {
        BusinessCategoriesUr[index]
    } else {
        category
    }
}

fun getPaymentMethodLabel(method: String, isUrdu: Boolean): String {
    val clean = method.uppercase(Locale.US)
    return when {
        clean.contains("CASH") -> if (isUrdu) "نقد (کیش)" else "Cash"
        clean.contains("BANK") -> if (isUrdu) "بینک ٹرانسفر" else "Bank Transfer"
        clean.contains("JAZZ") -> if (isUrdu) "جاز کیش" else "JazzCash"
        clean.contains("EASY") -> if (isUrdu) "ایزی پیسہ" else "Easypaisa"
        clean.contains("CREDIT") || clean.contains("UDHAAR") -> if (isUrdu) "ادھار کھاتہ" else "Credit / Udhaar"
        clean.contains("DIGITAL") || clean.contains("CARD") -> if (isUrdu) "ڈیجیٹل / کارڈ" else "Card / Digital"
        clean.contains("OTHER") -> if (isUrdu) "دیگر" else "Other"
        else -> if (isUrdu) "دیگر ($method)" else method
    }
}

object Strings {
    fun appName(isUr: Boolean) = if (isUr) "سمارٹ شاپ" else "Smart Shop"
    fun tagline(isUr: Boolean) = if (isUr) "پاکستان کا اسمارٹ اور جدید ترین کاروباری نظام" else "Smart Retail Management"

    // Nav
    fun navDashboard(isUr: Boolean) = if (isUr) "ڈیش بورڈ" else "Dashboard"
    fun navPos(isUr: Boolean) = if (isUr) "بلنگ / POS" else "POS"
    fun navInventory(isUr: Boolean) = if (isUr) "اسٹاک" else "Inventory"
    fun navHub(isUr: Boolean) = if (isUr) "کھاتہ مرکز" else "Hub"
    fun navSettings(isUr: Boolean) = if (isUr) "ترتیبات" else "Settings"

    // Dashboard
    fun totalRevenue(isUr: Boolean) = if (isUr) "کل آمدنی (فروخت)" else "Total Sales Revenue"
    fun grossProfit(isUr: Boolean) = if (isUr) "مجموعی نفع (گراس پرافٹ)" else "Gross Profit"
    fun netProfit(isUr: Boolean) = if (isUr) "خالص نفع (نیٹ پرافٹ)" else "Net Profit"
    fun todaySales(isUr: Boolean) = if (isUr) "آج کی فروخت" else "Today's Sales"
    fun totalExpenses(isUr: Boolean) = if (isUr) "کل اخراجات" else "Total Expenses"
    fun receivables(isUr: Boolean) = if (isUr) "مارکیٹ ادھار وصولی" else "Receivables (Udhaar)"
    fun payables(isUr: Boolean) = if (isUr) "سپلائر کو واجب الادا" else "Payables Due"
    fun quickSale(isUr: Boolean) = if (isUr) "+ نیا بل / فروخت" else "+ New Sale"
    fun addProduct(isUr: Boolean) = if (isUr) "+ نیا آئٹم شامل کریں" else "+ Add Item"
    fun logExpense(isUr: Boolean) = if (isUr) "+ خرچہ درج کریں" else "+ Add Expense"
    fun receivePayment(isUr: Boolean) = if (isUr) "ادھار وصول کریں" else "Receive Payment"
    fun payVendor(isUr: Boolean) = if (isUr) "سپلائر کو ادائیگی" else "Pay Vendor"

    // POS
    fun posTitle(isUr: Boolean) = if (isUr) "پوائنٹ آف سیل (بلنگ کاؤنٹر)" else "Point of Sale Terminal"
    fun searchProducts(isUr: Boolean) = if (isUr) "پراڈکٹ کا نام، کوڈ یا بارکوڈ تلاش کریں..." else "Search product name, SKU, or barcode..."
    fun cartTitle(isUr: Boolean) = if (isUr) "گاہک کی ٹوکری" else "Order Cart"
    fun emptyCart(isUr: Boolean) = if (isUr) "ٹوکری خالی ہے، اوپر سے آئٹم منتخب کریں" else "Cart is empty. Tap any item above to add"
    fun subtotal(isUr: Boolean) = if (isUr) "ذیلی رقم (سب ٹوٹل)" else "Subtotal"
    fun discount(isUr: Boolean) = if (isUr) "رعایت (ڈسکاؤنٹ)" else "Discount"
    fun tax(isUr: Boolean) = if (isUr) "سیلز ٹیکس" else "Sales Tax"
    fun totalDue(isUr: Boolean) = if (isUr) "کل واجب الادا رقم" else "Total Due"
    fun checkout(isUr: Boolean) = if (isUr) "بل فائنل کریں اور پرنٹ لیں" else "Proceed to Settle"
    fun cashReceived(isUr: Boolean) = if (isUr) "وصول شدہ کیش" else "Cash Received"
    fun changeDue(isUr: Boolean) = if (isUr) "گاہک کا بقایا" else "Change Due"
    fun clientName(isUr: Boolean) = if (isUr) "گاہک کا نام (اختیاری)" else "Customer Name (Optional)"
    fun paymentMethod(isUr: Boolean) = if (isUr) "طریقہ ادائیگی" else "Payment Method"

    // Invoices
    fun invoiceTitle(isUr: Boolean) = if (isUr) "سیلز ٹیکس انوائس" else "TAX INVOICE"
    fun invoiceNumber(isUr: Boolean) = if (isUr) "بل نمبر" else "Invoice No"
    fun sharePdf(isUr: Boolean) = if (isUr) "پی ڈی ایف انوائس شیئر کریں" else "Share Invoice PDF"
    fun shareText(isUr: Boolean) = if (isUr) "رسید ایس ایم ایس / واٹس ایپ کریں" else "Share Text Receipt"
    fun date(isUr: Boolean) = if (isUr) "تاریخ و وقت" else "Date & Time"

    // Customers
    fun customersTitle(isUr: Boolean) = if (isUr) "کسٹمر کھاتہ و ادھار رجسٹر" else "Customer Ledger & Accounts"
    fun addCustomer(isUr: Boolean) = if (isUr) "+ نیا کسٹمر شامل کریں" else "+ Add Customer"
    fun customerName(isUr: Boolean) = if (isUr) "کسٹمر کا نام" else "Customer Name"
    fun phone(isUr: Boolean) = if (isUr) "موبائل فون نمبر (03XXXXXXXXX یا +923XXXXXXXXX)" else "Phone Number (03XXXXXXXXX or +923XXXXXXXXX)"
    fun balanceDue(isUr: Boolean) = if (isUr) "بقایا ادھار رقم" else "Balance Due"
    fun totalSpent(isUr: Boolean) = if (isUr) "کل خریداری" else "Total Purchased"

    // Suppliers
    fun suppliersTitle(isUr: Boolean) = if (isUr) "سپلائرز و وینڈرز رجسٹر" else "Supplier Directory"
    fun addSupplier(isUr: Boolean) = if (isUr) "+ نیا سپلائر شامل کریں" else "+ Add Vendor"
    fun companyName(isUr: Boolean) = if (isUr) "کمپنی / وینڈر کا نام" else "Company / Vendor Name"
    fun payableDue(isUr: Boolean) = if (isUr) "سپلائر کا واجب الادا بیلنس" else "Payable Due"

    // Expenses
    fun expensesTitle(isUr: Boolean) = if (isUr) "کاروباری اخراجات کا رجسٹر" else "Business Expenses Ledger"
    fun todayOverhead(isUr: Boolean) = if (isUr) "آج کے اخراجات" else "Today's Overhead"
    fun monthlyOverhead(isUr: Boolean) = if (isUr) "اس مہینے کے کل اخراجات" else "Month-to-Date"
    fun expenseTitle(isUr: Boolean) = if (isUr) "خرچے کی تفصیل" else "Expense Description"
    fun category(isUr: Boolean) = if (isUr) "کیٹیگری" else "Category"

    // Reports
    fun reportsTitle(isUr: Boolean) = if (isUr) "کاروباری رپورٹس اور نفع نقصان" else "Executive Financial Reports"
    fun stockValuation(isUr: Boolean) = if (isUr) "گودام مال کی کل مالیت" else "Inventory Stock Valuation"
    fun costValue(isUr: Boolean) = if (isUr) "خریداری کی قیمت پر مالیت" else "Asset Cost Valuation"
    fun retailValue(isUr: Boolean) = if (isUr) "فروخت کی قیمت پر مالیت" else "Retail Potential Valuation"
    fun bestSellers(isUr: Boolean) = if (isUr) "سب سے زیادہ فروخت ہونے والے آئٹمز" else "Best-Selling Products"

    // Settings
    fun settingsTitle(isUr: Boolean) = if (isUr) "سسٹم سیٹنگز و ترتیبات" else "System Settings"
    fun businessProfile(isUr: Boolean) = if (isUr) "کاروباری پروفائل" else "Business Profile"
    fun businessAddress(isUr: Boolean) = if (isUr) "دکان / کاروبار کا پتہ" else "Business Address"
    fun ntnNumber(isUr: Boolean) = if (isUr) "این ٹی این / ٹیکس رجسٹریشن نمبر" else "NTN / Tax Registration"
    fun language(isUr: Boolean) = if (isUr) "زبان منتخب کریں" else "Language Selection"
    fun currency(isUr: Boolean) = if (isUr) "کرنسی (پاکستانی روپیہ ڈیفالٹ)" else "Currency (PKR Default)"
    fun appLock(isUr: Boolean) = if (isUr) "ایپ پن لاک سیکیورٹی" else "App PIN Lock"
    fun backupRestore(isUr: Boolean) = if (isUr) "ڈیٹا بیک اپ اور بحالی" else "Backup & Restore Vault"
    fun exportBackup(isUr: Boolean) = if (isUr) "آف لائن بیک اپ بنائیں" else "Export Offline Backup"
    fun dangerZone(isUr: Boolean) = if (isUr) "خطرناک زون (ڈیٹا ری سیٹ)" else "Danger Zone (Data Reset)"
    fun saveChanges(isUr: Boolean) = if (isUr) "تبدیلیاں محفوظ کریں" else "Save Changes"
}
