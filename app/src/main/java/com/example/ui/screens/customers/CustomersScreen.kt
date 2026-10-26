package com.example.ui.screens.customers

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.CustomerEntity
import com.example.util.WhatsAppShareHelper
import com.example.data.local.entity.CustomerPaymentEntity
import com.example.localization.KarobarFormatter
import com.example.localization.KarobarMath
import com.example.localization.KarobarPaymentMethods
import com.example.localization.getPaymentMethodLabel
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
import com.example.ui.theme.ShopSecondary
import java.util.Locale

@Composable
fun CustomersScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    var isAddingCustomer by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToReceivePayment by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    val currency = uiState.settings.currencySymbol
    val isUrdu = uiState.settings.language == "ur"
    val totalReceivables = uiState.metrics.totalReceivables

    val filteredCustomers = remember(uiState.allCustomers, searchQuery) {
        uiState.allCustomers.filter { customer ->
            searchQuery.isBlank() ||
                    customer.name.contains(searchQuery, ignoreCase = true) ||
                    customer.phone.contains(searchQuery, ignoreCase = true) ||
                    customer.email.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .testTag("customers_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title Bar & Add Customer Action - clean single title, no tagline
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUrdu) "کسٹمر کھاتہ" else "Customers",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                FuturisticButton(
                    text = if (isUrdu) "+ نیا کسٹمر" else "+ Add Client",
                    icon = Icons.Default.PersonAdd,
                    onClick = { isAddingCustomer = true },
                    testTag = "btn_add_customer"
                )
            }
        }

        // Outstanding Receivables Overview Card
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = if (totalReceivables > 0) ShopAmberAccent else ShopEmeraldAccent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Customer Receivables (Udhaar)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = KarobarFormatter.formatCurrency(totalReceivables, currency),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if (totalReceivables > 0) ShopAmberAccent else ShopEmeraldAccent
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                (if (totalReceivables > 0) ShopAmberAccent else ShopEmeraldAccent).copy(alpha = 0.2f)
                            )
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = if (totalReceivables > 0) ShopAmberAccent else ShopEmeraldAccent,
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
                placeholder = { Text("Search by customer name, phone, or email...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShopPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Customer Cards List
        if (filteredCustomers.isEmpty()) {
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No customers found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Add new client profiles to track ledger balances and payment records.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredCustomers, key = { it.id }) { customer ->
                CustomerCard(
                    customer = customer,
                    currency = currency,
                    onReceivePayment = { customerToReceivePayment = customer },
                    onSendReminder = {
                        WhatsAppShareHelper.sendUdhaarReminder(
                            context = context,
                            customer = customer,
                            storeName = uiState.settings.storeName.ifBlank { "Smart Shop" },
                            currencySymbol = currency
                        )
                    },
                    onEdit = { customerToEdit = customer },
                    onDelete = { customerToDelete = customer }
                )
            }
        }
    }

    // Add Customer Dialog
    if (isAddingCustomer) {
        CustomerFormDialog(
            initialCustomer = null,
            onDismiss = { isAddingCustomer = false },
            onSave = { newCust -> viewModel.addCustomer(newCust) }
        )
    }

    // Edit Customer Dialog
    customerToEdit?.let { customer ->
        CustomerFormDialog(
            initialCustomer = customer,
            onDismiss = { customerToEdit = null },
            onSave = { updatedCust -> viewModel.updateCustomer(updatedCust) }
        )
    }

    // Receive Payment Dialog
    customerToReceivePayment?.let { customer ->
        ReceivePaymentDialog(
            customer = customer,
            currency = currency,
            onDismiss = { customerToReceivePayment = null },
            onRecordPayment = { payment -> viewModel.recordCustomerPayment(payment) }
        )
    }

    // Delete Confirmation Dialog
    customerToDelete?.let { cust ->
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("Delete Customer Profile?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${cust.name}? Outstanding balance: $currency${cust.outstandingBalance}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomer(cust.id)
                        customerToDelete = null
                    }
                ) {
                    Text("Delete", color = ShopRoseAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun CustomerCard(
    customer: CustomerEntity,
    currency: String,
    onReceivePayment: () -> Unit,
    onSendReminder: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val hasOutstanding = customer.outstandingBalance > 0

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_card_${customer.id}"),
        glowColor = if (hasOutstanding) ShopAmberAccent else ShopPrimary,
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                (if (hasOutstanding) ShopAmberAccent else ShopPrimary).copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customer.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (hasOutstanding) ShopAmberAccent else ShopPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = ShopCyanAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = customer.phone,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (customer.email.isNotBlank()) {
                            Text(
                                text = customer.email,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Balance Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Balance Due",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = KarobarFormatter.formatCurrency(customer.outstandingBalance, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (hasOutstanding) ShopAmberAccent else ShopEmeraldAccent
                    )
                    Text(
                        text = "Total Spent: ${KarobarFormatter.formatCurrency(customer.totalPurchases, currency)}",
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
                if (hasOutstanding) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(ShopEmeraldAccent.copy(alpha = 0.18f))
                                .border(1.dp, ShopEmeraldAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable(onClick = onReceivePayment)
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = ShopEmeraldAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Receive",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ShopEmeraldAccent
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(ShopAmberAccent.copy(alpha = 0.18f))
                                .border(1.dp, ShopAmberAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable(onClick = onSendReminder)
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "WhatsApp / SMS Reminder",
                                    tint = ShopAmberAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "WhatsApp Reminder",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ShopAmberAccent
                                )
                            }
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
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ShopRoseAccent.copy(alpha = 0.15f))
                            .clickable(onClick = onDelete)
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = ShopRoseAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerFormDialog(
    initialCustomer: CustomerEntity? = null,
    onDismiss: () -> Unit,
    onSave: (CustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialCustomer?.name ?: "") }
    var phone by remember { mutableStateOf(initialCustomer?.phone ?: "") }
    var email by remember { mutableStateOf(initialCustomer?.email ?: "") }
    var address by remember { mutableStateOf(initialCustomer?.address ?: "") }
    var balanceText by remember {
        mutableStateOf(if (initialCustomer != null) String.format(Locale.US, "%.2f", initialCustomer.outstandingBalance) else "0.00")
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
                        text = if (initialCustomer != null) "Edit Customer Profile" else "Add New Customer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    placeholder = { Text("+92 300 1234567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / City (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Initial Outstanding Balance") },
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
                    text = "Save Customer",
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage = "Name cannot be empty"
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

                        val cust = CustomerEntity(
                            id = initialCustomer?.id ?: 0,
                            name = name.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            address = address.trim(),
                            outstandingBalance = bal,
                            totalPurchases = initialCustomer?.totalPurchases ?: 0.0
                        )
                        onSave(cust)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ReceivePaymentDialog(
    customer: CustomerEntity,
    currency: String,
    onDismiss: () -> Unit,
    onRecordPayment: (CustomerPaymentEntity) -> Unit
) {
    var amountText by remember {
        mutableStateOf(String.format(Locale.US, "%.2f", customer.outstandingBalance))
    }
    var selectedMethod by remember { mutableStateOf("CASH") }
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
                        Text(text = "Receive Customer Payment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "${customer.name} • Due: $currency${customer.outstandingBalance}", style = MaterialTheme.typography.labelSmall, color = ShopAmberAccent)
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
                            else -> Icons.Default.MoreHoriz
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
                    label = { Text("Payment Note / Slip # (Optional)") },
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
                    text = "Confirm & Settle Balance",
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            errorMessage = "Enter a valid payment amount"
                            return@FuturisticButton
                        }
                        val payment = CustomerPaymentEntity(
                            customerId = customer.id,
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
