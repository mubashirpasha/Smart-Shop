package com.example.ui.screens.suppliers

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.SupplierPaymentEntity
import com.example.localization.KarobarFormatter
import com.example.localization.KarobarMath
import com.example.localization.KarobarPaymentMethods
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.MoreHoriz
import com.example.ui.components.FuturisticButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.theme.ShopAmberAccent
import com.example.ui.theme.ShopBorderDark
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopRoseAccent
import java.util.Locale

@Composable
fun SuppliersScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    BackHandler { onNavigateBack() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    var isAddingSupplier by remember { mutableStateOf(false) }
    var supplierToEdit by remember { mutableStateOf<SupplierEntity?>(null) }
    var supplierToPay by remember { mutableStateOf<SupplierEntity?>(null) }
    var supplierToDelete by remember { mutableStateOf<SupplierEntity?>(null) }

    val currency = uiState.settings.currencySymbol
    val isUrdu = uiState.settings.language == "ur"
    val totalPayables = uiState.metrics.totalPayables

    val filteredSuppliers = remember(uiState.allSuppliers, searchQuery) {
        uiState.allSuppliers.filter { supplier ->
            searchQuery.isBlank() ||
                    supplier.name.contains(searchQuery, ignoreCase = true) ||
                    supplier.companyName.contains(searchQuery, ignoreCase = true) ||
                    supplier.phone.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .testTag("suppliers_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title Bar & Add Supplier Button - clean single title, no tagline
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUrdu) "سپلائرز" else "Suppliers",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                FuturisticButton(
                    text = if (isUrdu) "+ نیا سپلائر" else "+ Add Vendor",
                    icon = Icons.Default.Business,
                    onClick = { isAddingSupplier = true },
                    testTag = "btn_add_supplier"
                )
            }
        }

        // Outstanding Payables Overview Card
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = if (totalPayables > 0) ShopRoseAccent else ShopEmeraldAccent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Accounts Payable (Supplier Dues)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = KarobarFormatter.formatCurrency(totalPayables, currency),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if (totalPayables > 0) ShopRoseAccent else ShopEmeraldAccent
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                (if (totalPayables > 0) ShopRoseAccent else ShopEmeraldAccent).copy(alpha = 0.2f)
                            )
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = if (totalPayables > 0) ShopRoseAccent else ShopEmeraldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                placeholder = { Text("Search by vendor, company name, or phone...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("supplier_search_input"),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Supplier Cards
        if (filteredSuppliers.isEmpty()) {
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "No suppliers found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Add vendor records to keep track of stock purchases and payments.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredSuppliers, key = { it.id }) { supplier ->
                SupplierCard(
                    supplier = supplier,
                    currency = currency,
                    onPaySupplier = { supplierToPay = supplier },
                    onEdit = { supplierToEdit = supplier },
                    onDelete = { supplierToDelete = supplier }
                )
            }
        }
    }

    // Add Supplier Dialog
    if (isAddingSupplier) {
        SupplierFormDialog(
            initialSupplier = null,
            onDismiss = { isAddingSupplier = false },
            onSave = { newSupp -> viewModel.addSupplier(newSupp) }
        )
    }

    // Edit Supplier Dialog
    supplierToEdit?.let { supplier ->
        SupplierFormDialog(
            initialSupplier = supplier,
            onDismiss = { supplierToEdit = null },
            onSave = { updatedSupp -> viewModel.updateSupplier(updatedSupp) }
        )
    }

    // Record Supplier Payment Dialog
    supplierToPay?.let { supplier ->
        SupplierPaymentDialog(
            supplier = supplier,
            currency = currency,
            onDismiss = { supplierToPay = null },
            onRecordPayment = { payment -> viewModel.recordSupplierPayment(payment) }
        )
    }

    // Delete Confirmation
    supplierToDelete?.let { supp ->
        AlertDialog(
            onDismissRequest = { supplierToDelete = null },
            title = { Text("Delete Supplier Profile?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove '${supp.companyName.ifBlank { supp.name }}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSupplier(supp.id)
                        supplierToDelete = null
                    }
                ) {
                    Text("Delete", color = ShopRoseAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { supplierToDelete = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun SupplierCard(
    supplier: SupplierEntity,
    currency: String,
    onPaySupplier: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val hasPayables = supplier.outstandingBalance > 0

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("supplier_card_${supplier.id}"),
        glowColor = if (hasPayables) ShopRoseAccent else ShopCyanAccent,
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                (if (hasPayables) ShopRoseAccent else ShopCyanAccent).copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = if (hasPayables) ShopRoseAccent else ShopCyanAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = supplier.companyName.ifBlank { supplier.name },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Rep: ${supplier.name} • ${supplier.phone}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (supplier.address.isNotBlank()) {
                            Text(
                                text = supplier.address,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Balance Due
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Payable Due",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = KarobarFormatter.formatCurrency(supplier.outstandingBalance, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (hasPayables) ShopRoseAccent else ShopEmeraldAccent
                    )
                    Text(
                        text = "Lifetime: ${KarobarFormatter.formatCurrency(supplier.totalPurchased, currency)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasPayables) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ShopCyanAccent.copy(alpha = 0.18f))
                            .border(1.dp, ShopCyanAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable(onClick = onPaySupplier)
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = ShopCyanAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Pay Vendor", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ShopCyanAccent)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = onEdit)
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(14.dp))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ShopRoseAccent.copy(alpha = 0.15f))
                            .clickable(onClick = onDelete)
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ShopRoseAccent, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplierFormDialog(
    initialSupplier: SupplierEntity? = null,
    onDismiss: () -> Unit,
    onSave: (SupplierEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialSupplier?.name ?: "") }
    var companyName by remember { mutableStateOf(initialSupplier?.companyName ?: "") }
    var phone by remember { mutableStateOf(initialSupplier?.phone ?: "") }
    var email by remember { mutableStateOf(initialSupplier?.email ?: "") }
    var address by remember { mutableStateOf(initialSupplier?.address ?: "") }
    var balanceText by remember {
        mutableStateOf(if (initialSupplier != null) String.format(Locale.US, "%.2f", initialSupplier.outstandingBalance) else "0.00")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialSupplier != null) "Edit Vendor Profile" else "Add New Vendor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company / Vendor Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Person / Representative *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Business Address / City") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Initial Outstanding Payable") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.height(18.dp))

                FuturisticButton(
                    text = "Save Vendor",
                    onClick = {
                        if (companyName.isBlank() && name.isBlank()) {
                            errorMessage = "Vendor or Contact Name is required"
                            return@FuturisticButton
                        }
                        if (phone.isBlank()) {
                            errorMessage = "Phone number is required"
                            return@FuturisticButton
                        }
                        val phoneCheck = KarobarFormatter.validatePakistaniPhone(phone)
                        if (!phoneCheck.isValid) {
                            errorMessage = phoneCheck.errorMessageEn
                            return@FuturisticButton
                        }
                        val bal = balanceText.toDoubleOrNull() ?: 0.0

                        val supp = SupplierEntity(
                            id = initialSupplier?.id ?: 0,
                            name = name.trim().ifBlank { companyName.trim() },
                            companyName = companyName.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            address = address.trim(),
                            outstandingBalance = bal,
                            totalPurchased = initialSupplier?.totalPurchased ?: 0.0
                        )
                        onSave(supp)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SupplierPaymentDialog(
    supplier: SupplierEntity,
    currency: String,
    onDismiss: () -> Unit,
    onRecordPayment: (SupplierPaymentEntity) -> Unit
) {
    var amountText by remember {
        mutableStateOf(String.format(Locale.US, "%.2f", supplier.outstandingBalance))
    }
    var selectedMethod by remember { mutableStateOf("BANK") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Pay Supplier / Vendor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "${supplier.companyName.ifBlank { supplier.name }} • Due: $currency${supplier.outstandingBalance}", style = MaterialTheme.typography.labelSmall, color = ShopRoseAccent)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 6 Selectable Pakistani Payment Methods
                LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(KarobarPaymentMethods) { method ->
                        val isSelected = selectedMethod == method.code
                        val icon = when (method.code) {
                            "CASH" -> Icons.Default.Payments
                            "BANK" -> Icons.Default.AccountBalance
                            "JAZZCASH" -> Icons.Default.PhoneAndroid
                            "EASYPAISA" -> Icons.Default.PhoneAndroid
                            "CREDIT" -> Icons.Default.AccountBalanceWallet
                            else -> Icons.Default.Business
                        }
                        val accent = Color(method.colorHex)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) accent.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, if (isSelected) accent else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { selectedMethod = method.code }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = icon, contentDescription = method.nameEn, tint = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = method.nameEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Cheque / Bank Reference (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.height(18.dp))

                FuturisticButton(
                    text = "Confirm Payment",
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            errorMessage = "Enter a valid payment amount"
                            return@FuturisticButton
                        }
                        val payment = SupplierPaymentEntity(
                            supplierId = supplier.id,
                            amount = amount,
                            paymentMethod = selectedMethod,
                            note = note.trim()
                        )
                        onRecordPayment(payment)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
