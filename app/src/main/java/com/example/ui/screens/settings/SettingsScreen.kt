package com.example.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.preferences.ThemeMode
import com.example.data.repository.ShopFlowRepository
import com.example.localization.BusinessCategoriesEn
import com.example.localization.BusinessCategoriesUr
import com.example.localization.KarobarFormatter
import com.example.ui.components.FuturisticButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.theme.ShopAmberAccent
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopRoseAccent
import com.example.ui.theme.ShopSecondary
import com.example.ui.theme.ShopSuccessBackground
import kotlinx.coroutines.launch

data class CurrencyOption(val symbol: String, val name: String, val code: String = "PKR")

val AvailableCurrencies = listOf(
    CurrencyOption("Rs.", "PKR (Pakistani Rupee - Default)", "PKR"),
    CurrencyOption("$", "USD (US Dollar)", "USD"),
    CurrencyOption("€", "EUR (Euro)", "EUR"),
    CurrencyOption("£", "GBP (British Pound)", "GBP"),
    CurrencyOption("AED", "AED (UAE Dirham)", "AED"),
    CurrencyOption("SAR", "SAR (Saudi Riyal)", "SAR"),
    CurrencyOption("₹", "INR (Indian Rupee)", "INR"),
    CurrencyOption("¥", "CNY (Chinese Yuan)", "CNY")
)

data class InvoiceTemplateOption(val id: String, val nameEn: String, val nameUr: String, val descEn: String)

val InvoiceTemplates = listOf(
    InvoiceTemplateOption("STANDARD", "Standard Business", "معیاری انوائس", "Clean tax invoice format with branding"),
    InvoiceTemplateOption("THERMAL", "Thermal Receipt (80mm)", "تھرمل پرچی (80 ملی میٹر)", "Compact receipt suitable for POS thermal printers"),
    InvoiceTemplateOption("DETAILED", "Detailed Tax Invoice", "تفصیلی سیلز ٹیکس بل", "Full itemized breakdown with tax & customer details")
)

@Composable
fun SettingsScreen(
    repository: ShopFlowRepository,
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onLockAppNow: (() -> Unit)? = null
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = uiState.settings
    val isUrdu = settings.language == "ur"

    // Form states
    var storeName by remember { mutableStateOf(settings.storeName) }
    var phone by remember { mutableStateOf(settings.businessPhone) }
    var address by remember { mutableStateOf(settings.businessAddress) }
    var ntn by remember { mutableStateOf(settings.businessNtn) }
    var businessCategory by remember { mutableStateOf(settings.businessCategory) }
    var customCategoryText by remember { mutableStateOf("") }
    var isCustomCategoryMode by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    // Invoice Customization states
    var selectedTemplate by remember { mutableStateOf(settings.invoiceTemplate) }
    var invoiceFooter by remember { mutableStateOf(settings.invoiceFooter) }
    var showTaxNumber by remember { mutableStateOf(settings.invoiceShowTaxNumber) }
    var showCustomerPhone by remember { mutableStateOf(settings.invoiceShowCustomerPhone) }
    var showShopAddress by remember { mutableStateOf(settings.invoiceShowShopAddress) }

    // Async saving states
    var isSavingProfile by remember { mutableStateOf(false) }
    var isSavingInvoice by remember { mutableStateOf(false) }
    var hasInitializedForm by remember { mutableStateOf(false) }

    // Synchronize form values only once upon initial load from settings
    LaunchedEffect(settings) {
        if (!hasInitializedForm) {
            storeName = settings.storeName
            phone = settings.businessPhone
            address = settings.businessAddress
            ntn = settings.businessNtn
            val isKnown = BusinessCategoriesEn.contains(settings.businessCategory)
            if (isKnown) {
                businessCategory = settings.businessCategory
                isCustomCategoryMode = false
            } else if (settings.businessCategory.isNotBlank()) {
                businessCategory = "Other"
                customCategoryText = settings.businessCategory
                isCustomCategoryMode = true
            } else {
                businessCategory = "General Store"
                isCustomCategoryMode = false
            }

            selectedTemplate = settings.invoiceTemplate
            invoiceFooter = settings.invoiceFooter
            showTaxNumber = settings.invoiceShowTaxNumber
            showCustomerPhone = settings.invoiceShowCustomerPhone
            showShopAddress = settings.invoiceShowShopAddress

            hasInitializedForm = true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Title
        item {
            Text(
                text = if (isUrdu) "ترتیبات" else "Settings",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // 1. Business Profile Section
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                testTag = "card_business_profile"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ShopPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = ShopPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isUrdu) "کاروباری پروفائل" else "Business Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Business / Store Name
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text(if (isUrdu) "دکان یا کاروبار کا نام" else "Business / Store Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Business Category Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isUrdu) "کاروبار کی کیٹیگری" else "Business Category",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val categories = if (isUrdu) BusinessCategoriesUr else BusinessCategoriesEn
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { cat ->
                                val isSelected = (!isCustomCategoryMode && (businessCategory == cat || (isUrdu && BusinessCategoriesUr.indexOf(cat) == BusinessCategoriesEn.indexOf(businessCategory)))) || (cat == (if (isUrdu) "دیگر" else "Other") && isCustomCategoryMode)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) ShopSuccessBackground else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, if (isSelected) ShopPrimary else Color.Transparent, RoundedCornerShape(10.dp))
                                        .clickable {
                                            if (cat == "Other" || cat == "دیگر") {
                                                isCustomCategoryMode = true
                                            } else {
                                                isCustomCategoryMode = false
                                                businessCategory = if (isUrdu) {
                                                    val idx = BusinessCategoriesUr.indexOf(cat)
                                                    if (idx in BusinessCategoriesEn.indices) BusinessCategoriesEn[idx] else cat
                                                } else {
                                                    cat
                                                }
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) ShopPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (isCustomCategoryMode) {
                            OutlinedTextField(
                                value = customCategoryText,
                                onValueChange = { customCategoryText = it },
                                label = { Text(if (isUrdu) "اپنی مرضی کی کیٹیگری" else "Custom Business Category") },
                                placeholder = { Text(if (isUrdu) "مثلاً: آٹو پارٹس، بیکری، کتابوں کی دکان" else "e.g., Auto Parts, Bakery, Stationery") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Contact Phone
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = null
                        },
                        label = { Text(if (isUrdu) "رابطہ فون نمبر" else "Contact Phone") },
                        placeholder = { Text("0300 1234567") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = phoneError != null,
                        supportingText = if (phoneError != null) {
                            { Text(text = phoneError ?: "", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Store Address / City (multi-line textarea allowing long addresses to wrap naturally)
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(if (isUrdu) "دکان کا پتہ اور شہر" else "Store Address / City") },
                        placeholder = { Text(if (isUrdu) "مثلاً: دکان نمبر 4، مین مارکیٹ، لاہور" else "e.g., Shop #4, Main Market, Lahore") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Tax / NTN Registration Number (Optional)
                    OutlinedTextField(
                        value = ntn,
                        onValueChange = { ntn = it },
                        label = { Text(if (isUrdu) "ٹیکس / NTN رجسٹریشن نمبر (اختیاری)" else "Tax / NTN Registration Number (Optional)") },
                        placeholder = { Text(if (isUrdu) "مثلاً: 1234567-8 (اختیاری)" else "e.g., 1234567-8 (Optional)") },
                        supportingText = {
                            Text(
                                text = if (isUrdu) "غیر رجسٹرڈ کاروبار کے لیے لازمی نہیں ہے" else "Optional for unregistered businesses",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Save Business Profile Action with asynchronous verification
                    FuturisticButton(
                        text = if (isSavingProfile) (if (isUrdu) "محفوظ کیا جا رہا ہے..." else "Saving...") else (if (isUrdu) "پروفائل محفوظ کریں" else "Save Business Profile"),
                        icon = Icons.Default.Save,
                        onClick = {
                            if (isSavingProfile) return@FuturisticButton

                            if (phone.isNotBlank()) {
                                val validation = KarobarFormatter.validatePakistaniPhone(phone)
                                if (!validation.isValid) {
                                    phoneError = if (isUrdu) validation.errorMessageUr else validation.errorMessageEn
                                    return@FuturisticButton
                                }
                            }

                            val finalCategory = if (isCustomCategoryMode && customCategoryText.isNotBlank()) {
                                customCategoryText.trim()
                            } else {
                                businessCategory
                            }

                            isSavingProfile = true
                            viewModel.saveBusinessProfile(
                                storeName = storeName.trim(),
                                category = finalCategory,
                                phone = phone.trim(),
                                address = address.trim(),
                                ntn = ntn.trim(),
                                onSuccess = {
                                    isSavingProfile = false
                                    Toast.makeText(context, if (isUrdu) "کاروباری پروفائل کامیابی سے محفوظ ہوگئی" else "Business Profile saved successfully", Toast.LENGTH_SHORT).show()
                                },
                                onError = { errorMsg ->
                                    isSavingProfile = false
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }


        // 4. Language Options (English & Urdu)
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                testTag = "card_language_options"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ShopPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = ShopPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = if (isUrdu) "زبان کا انتخاب" else "Language / زبان", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val languages = listOf("en" to "English (US)", "ur" to "اردو (Urdu)")
                        languages.forEach { (code, label) ->
                            val isSelected = settings.language == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) ShopPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.updateLanguage(code) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Interface Appearance (Light Mode / Dark Mode)
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                testTag = "card_theme_options"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ShopPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = ShopPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isUrdu) "ایپ کا تھیم اور انداز" else "Interface Appearance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val themeOptions = if (isUrdu) {
                        listOf(ThemeMode.LIGHT to "لائٹ موڈ", ThemeMode.DARK to "ڈارک نیوی", ThemeMode.SYSTEM to "سسٹم")
                    } else {
                        listOf(ThemeMode.LIGHT to "Light Mode", ThemeMode.DARK to "Dark Navy", ThemeMode.SYSTEM to "System")
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        themeOptions.forEach { (mode, label) ->
                            val isSelected = settings.themeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) ShopPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.updateTheme(mode) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Invoice Customization & Template Settings
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                testTag = "card_invoice_settings"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ShopPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = ShopPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isUrdu) "انوائس و بل کی سیٹنگز" else "Invoice & Template Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = if (isUrdu) "اپنی دکان کے بل کا انداز اور ضروری تفصیلات منتخب کریں۔"
                        else "Customize receipt template, printable invoice header options, and legal disclaimer footer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Template Options
                    Text(
                        text = if (isUrdu) "انوائس کا فارمیٹ" else "Invoice Template",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InvoiceTemplates.forEach { tpl ->
                            val isSelected = selectedTemplate == tpl.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ShopSuccessBackground else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .border(1.dp, if (isSelected) ShopPrimary else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable { selectedTemplate = tpl.id }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isUrdu) tpl.nameUr else tpl.nameEn,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSelected) ShopPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = tpl.descEn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ShopPrimary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Toggles for invoice display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isUrdu) "بل پر ٹیکس / NTN نمبر دکھائیں" else "Show Tax / NTN Number on Invoice",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = showTaxNumber,
                            onCheckedChange = { showTaxNumber = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ShopPrimary, checkedTrackColor = ShopSuccessBackground)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isUrdu) "بل پر گاہک کا فون نمبر دکھائیں" else "Show Customer Phone on Invoice",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = showCustomerPhone,
                            onCheckedChange = { showCustomerPhone = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ShopPrimary, checkedTrackColor = ShopSuccessBackground)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isUrdu) "بل پر دکان کا پتہ اور فون دکھائیں" else "Show Store Address & Phone on Invoice",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = showShopAddress,
                            onCheckedChange = { showShopAddress = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ShopPrimary, checkedTrackColor = ShopSuccessBackground)
                        )
                    }

                    // Invoice Footer Textarea
                    OutlinedTextField(
                        value = invoiceFooter,
                        onValueChange = { invoiceFooter = it },
                        label = { Text(if (isUrdu) "انوائس کا فوٹر نوٹ" else "Invoice Footer Disclaimer") },
                        placeholder = { Text("Thank you for your business! Computerized tax invoice generated by KarobarFlow.") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Save Invoice Settings Button
                    FuturisticButton(
                        text = if (isSavingInvoice) (if (isUrdu) "محفوظ کیا جا رہا ہے..." else "Saving...") else (if (isUrdu) "انوائس سیٹنگز محفوظ کریں" else "Save Invoice Settings"),
                        icon = Icons.Default.Save,
                        onClick = {
                            if (isSavingInvoice) return@FuturisticButton
                            isSavingInvoice = true
                            viewModel.saveInvoiceSettings(
                                template = selectedTemplate,
                                footer = invoiceFooter.trim(),
                                showTax = showTaxNumber,
                                showPhone = showCustomerPhone,
                                showAddress = showShopAddress,
                                onSuccess = {
                                    isSavingInvoice = false
                                    Toast.makeText(context, if (isUrdu) "انوائس کی سیٹنگز محفوظ ہوگئیں" else "Invoice settings saved successfully", Toast.LENGTH_SHORT).show()
                                },
                                onError = { errorMsg ->
                                    isSavingInvoice = false
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }




    }
}

