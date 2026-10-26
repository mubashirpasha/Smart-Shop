package com.example.ui.screens.expenses

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.data.local.entity.ExpenseEntity
import com.example.localization.KarobarFormatter
import com.example.localization.KarobarMath
import com.example.localization.KarobarPaymentMethods
import com.example.localization.getPaymentMethodLabel
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import com.example.ui.components.FuturisticButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.theme.ShopAmberAccent
import com.example.ui.theme.ShopBorderDark
import com.example.ui.theme.ShopCyanAccent
import com.example.ui.theme.ShopEmeraldAccent
import com.example.ui.theme.ShopPrimary
import com.example.ui.theme.ShopRoseAccent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ExpensesScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    BackHandler { onNavigateBack() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    var isAddingExpense by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    val currency = uiState.settings.currencySymbol
    val isUrdu = uiState.settings.language == "ur"
    val totalExpenses = uiState.metrics.totalExpenses

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfToday = calendar.timeInMillis
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val startOfMonth = calendar.timeInMillis

    val todayExpensesTotal = remember(uiState.allExpenses) {
        uiState.allExpenses.filter { it.timestamp >= startOfToday }.sumOf { it.amount }
    }
    val monthlyExpensesTotal = remember(uiState.allExpenses) {
        uiState.allExpenses.filter { it.timestamp >= startOfMonth }.sumOf { it.amount }
    }

    val categories = listOf("All", "Rent", "Utilities", "Salaries", "Logistics", "Marketing", "Maintenance", "General")

    val filteredExpenses = remember(uiState.allExpenses, searchQuery, selectedCategory) {
        uiState.allExpenses.filter { exp ->
            val matchesQuery = searchQuery.isBlank() ||
                    exp.title.contains(searchQuery, ignoreCase = true) ||
                    exp.category.contains(searchQuery, ignoreCase = true) ||
                    exp.note.contains(searchQuery, ignoreCase = true)

            val matchesCat = selectedCategory == "All" || exp.category.equals(selectedCategory, ignoreCase = true)
            matchesQuery && matchesCat
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .testTag("expenses_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title & Add Expense - clean single title, no tagline
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUrdu) "کاروباری اخراجات" else "Expenses",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                FuturisticButton(
                    text = if (isUrdu) "نیا خرچہ" else "Add Expense",
                    icon = Icons.Default.Add,
                    onClick = { isAddingExpense = true },
                    testTag = "btn_add_expense"
                )
            }
        }

        // Daily and Monthly Summaries
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassmorphicCard(
                    modifier = Modifier.weight(1f),
                    glowColor = ShopRoseAccent
                ) {
                    Column {
                        Text(
                            text = "Today's Overhead",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = KarobarFormatter.formatCurrency(todayExpensesTotal, currency),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = ShopRoseAccent
                        )
                    }
                }

                GlassmorphicCard(
                    modifier = Modifier.weight(1f),
                    glowColor = ShopAmberAccent
                ) {
                    Column {
                        Text(
                            text = "Month-to-Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = KarobarFormatter.formatCurrency(monthlyExpensesTotal, currency),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = ShopAmberAccent
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
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                placeholder = { Text("Search expenses by title, category, note...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Category Filter Chips
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ShopPrimary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Expenses List
        if (filteredExpenses.isEmpty()) {
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.MoneyOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "No expenses recorded", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Add business expenses to calculate exact net profitability.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredExpenses, key = { it.id }) { expense ->
                ExpenseCard(
                    expense = expense,
                    currency = currency,
                    onDelete = { expenseToDelete = expense }
                )
            }
        }
    }

    // Add Expense Dialog
    if (isAddingExpense) {
        ExpenseFormDialog(
            currency = currency,
            onDismiss = { isAddingExpense = false },
            onSave = { newExp -> viewModel.addExpense(newExp) }
        )
    }

    // Delete Confirmation
    expenseToDelete?.let { exp ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete Expense Record?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove '${exp.title}' for $currency${exp.amount}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExpense(exp.id)
                        expenseToDelete = null
                    }
                ) {
                    Text("Delete", color = ShopRoseAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun ExpenseCard(
    expense: ExpenseEntity,
    currency: String,
    onDelete: () -> Unit
) {
    val formattedDate = remember(expense.timestamp) { KarobarFormatter.formatDateTime(expense.timestamp) }

    val categoryIcon = when (expense.category) {
        "Rent" -> Icons.Default.Home
        "Utilities" -> Icons.Default.Bolt
        "Salaries" -> Icons.Default.People
        "Logistics" -> Icons.Default.LocalShipping
        "Marketing" -> Icons.Default.Campaign
        "Maintenance" -> Icons.Default.Build
        else -> Icons.Default.Receipt
    }

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_card_${expense.id}"),
        glowColor = ShopRoseAccent,
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ShopRoseAccent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = categoryIcon, contentDescription = null, tint = ShopRoseAccent, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = expense.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = expense.category, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ShopCyanAccent)
                        Text(text = "• $formattedDate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (expense.note.isNotBlank()) {
                        Text(text = expense.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "-${KarobarFormatter.formatCurrency(expense.amount, currency)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = ShopRoseAccent
                    )
                    Text(text = getPaymentMethodLabel(expense.paymentMethod, false), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseFormDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Rent") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Rent", "Utilities", "Salaries", "Logistics", "Marketing", "Maintenance", "General")

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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Log Business Expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(imageVector = Icons.Default.Close, contentDescription = "Close") }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title / Purpose *") },
                    placeholder = { Text("e.g., Shop Electricity Bill") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($currency) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; dropdownExpanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 6 Selectable Pakistani Payment Methods
                LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(KarobarPaymentMethods) { method ->
                        val isSelected = paymentMethod == method.code
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
                                .clickable { paymentMethod = method.code }
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
                    label = { Text("Expense Note (Optional)") },
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
                    text = "Save Expense",
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "Title is required"
                            return@FuturisticButton
                        }
                        val amt = amountText.toDoubleOrNull()
                        if (amt == null || amt <= 0) {
                            errorMessage = "Enter a valid amount greater than 0"
                            return@FuturisticButton
                        }

                        val exp = ExpenseEntity(
                            title = title.trim(),
                            amount = amt,
                            category = category,
                            paymentMethod = paymentMethod,
                            note = note.trim()
                        )
                        onSave(exp)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
