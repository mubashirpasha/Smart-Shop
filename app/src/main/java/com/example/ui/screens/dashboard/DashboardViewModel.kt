package com.example.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.CategoryExpenseSummary
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.CustomerPaymentEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.SupplierPaymentEntity
import com.example.data.local.preferences.AppSettings
import com.example.data.local.preferences.ThemeMode
import com.example.data.repository.ShopFlowRepository
import com.example.ui.components.TimePeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class BestSellerItem(
    val productId: Long,
    val productName: String,
    val category: String,
    val unitsSold: Int,
    val totalRevenue: Double,
    val remainingStock: Int
)

data class DashboardMetrics(
    val todaySalesRevenue: Double = 0.0,
    val todaySalesCount: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalSalesCount: Int = 0,
    val grossProfit: Double = 0.0,
    val grossProfitMargin: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val totalReceivables: Double = 0.0,
    val totalPayables: Double = 0.0,
    val totalProducts: Int = 0,
    val totalStockUnits: Int = 0,
    val inventoryCostValue: Double = 0.0,
    val inventoryRetailValue: Double = 0.0,
    val lowStockCount: Int = 0,
    val periodRevenue: Double = 0.0,
    val periodSalesCount: Int = 0,
    val averageTicket: Double = 0.0,
    val todayAverageOrderValue: Double = 0.0,
    val trendPercentage: String? = null,
    val chartPoints: List<Float> = emptyList(),
    val chartLabels: List<String> = emptyList(),
    val bestSellers: List<BestSellerItem> = emptyList()
)

data class DashboardUiState(
    val isLoading: Boolean = false,
    val metrics: DashboardMetrics = DashboardMetrics(),
    val lowStockAlerts: List<ProductEntity> = emptyList(),
    val recentSales: List<SaleEntity> = emptyList(),
    val allSales: List<SaleEntity> = emptyList(),
    val allProducts: List<ProductEntity> = emptyList(),
    val allCustomers: List<CustomerEntity> = emptyList(),
    val allSuppliers: List<SupplierEntity> = emptyList(),
    val allExpenses: List<ExpenseEntity> = emptyList(),
    val expensesByCategory: List<CategoryExpenseSummary> = emptyList(),
    val selectedPeriod: TimePeriod = TimePeriod.TODAY,
    val settings: AppSettings = AppSettings()
)

private data class CommercialVaultData(
    val products: List<ProductEntity> = emptyList(),
    val sales: List<SaleEntity> = emptyList(),
    val saleItems: List<SaleItemEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList()
)

private data class StakeholderData(
    val customers: List<CustomerEntity> = emptyList(),
    val suppliers: List<SupplierEntity> = emptyList(),
    val expensesByCategory: List<CategoryExpenseSummary> = emptyList()
)

class DashboardViewModel(
    private val repository: ShopFlowRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(TimePeriod.TODAY)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    private val commercialFlow = combine(
        repository.allProducts,
        repository.allSales,
        repository.allSaleItems,
        repository.allExpenses
    ) { products, sales, saleItems, expenses ->
        CommercialVaultData(products, sales, saleItems, expenses)
    }

    private val stakeholderFlow = combine(
        repository.allCustomers,
        repository.allSuppliers,
        repository.expensesByCategory
    ) { customers, suppliers, expensesByCategory ->
        StakeholderData(customers, suppliers, expensesByCategory)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        commercialFlow,
        stakeholderFlow,
        _selectedPeriod,
        repository.appSettings
    ) { commercial, stakeholder, period, settings ->
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis
        val startOfWeek = currentTime - (7 * 86400_000L)
        val startOfMonth = currentTime - (30 * 86400_000L)

        val products = commercial.products
        val sales = commercial.sales
        val saleItems = commercial.saleItems
        val expenses = commercial.expenses

        val customers = stakeholder.customers
        val suppliers = stakeholder.suppliers
        val categoryExpenses = stakeholder.expensesByCategory

        // Today metrics
        val todaySales = sales.filter { it.timestamp >= startOfToday }
        val todaySalesRevenue = todaySales.sumOf { it.totalAmount }
        val todaySalesCount = todaySales.size

        // Total metrics
        val totalRevenue = sales.sumOf { it.totalAmount }
        val totalSalesCount = sales.size

        // Period filter metrics
        val periodStartTime = when (period) {
            TimePeriod.TODAY -> startOfToday
            TimePeriod.WEEK -> startOfWeek
            TimePeriod.MONTH -> startOfMonth
            TimePeriod.ALL -> 0L
        }
        val periodSales = if (periodStartTime == 0L) sales else sales.filter { it.timestamp >= periodStartTime }
        val periodRevenue = periodSales.sumOf { it.totalAmount }
        val periodSalesCount = periodSales.size
        val averageTicket = if (periodSalesCount > 0) periodRevenue / periodSalesCount else 0.0

        // Gross Profit Calculation
        val grossProfit = saleItems.sumOf { (it.unitPrice - it.costPrice) * it.quantity }
        val grossProfitMargin = if (totalRevenue > 0.0) (grossProfit / totalRevenue) * 100.0 else 0.0

        // Total Expenses & Net Profit
        val totalExpenses = expenses.sumOf { it.amount }
        val netProfit = grossProfit - totalExpenses

        // Customer Receivables & Supplier Payables
        val totalReceivables = customers.sumOf { it.outstandingBalance }
        val totalPayables = suppliers.sumOf { it.outstandingBalance }

        // Inventory Valuations
        val totalProducts = products.size
        val totalStockUnits = products.sumOf { it.stockQuantity }
        val inventoryCostValue = products.sumOf { it.purchasePrice * it.stockQuantity }
        val inventoryRetailValue = products.sumOf { it.sellingPrice * it.stockQuantity }

        // Low Stock items
        val lowStockAlerts = products.filter { it.stockQuantity <= it.minStockThreshold }

        // Best Sellers Calculation
        val itemsByProduct = saleItems.groupBy { it.productId }
        val bestSellers = itemsByProduct.map { (productId, items) ->
            val matchingProd = products.find { it.id == productId }
            val totalSold = items.sumOf { it.quantity }
            val itemRevenue = items.sumOf { it.subtotal }
            BestSellerItem(
                productId = productId,
                productName = matchingProd?.name ?: items.firstOrNull()?.productName ?: "Item #$productId",
                category = matchingProd?.category ?: "General",
                unitsSold = totalSold,
                totalRevenue = itemRevenue,
                remainingStock = matchingProd?.stockQuantity ?: 0
            )
        }.sortedByDescending { it.unitsSold }.take(5)

        // Animated Chart Points & Labels
        val (chartPoints, chartLabels) = generateChartData(period, sales, periodRevenue, totalRevenue)

        val todayAverageOrderValue = if (todaySalesCount > 0) todaySalesRevenue / todaySalesCount else 0.0

        val metrics = DashboardMetrics(
            todaySalesRevenue = todaySalesRevenue,
            todaySalesCount = todaySalesCount,
            totalRevenue = totalRevenue,
            totalSalesCount = totalSalesCount,
            grossProfit = grossProfit,
            grossProfitMargin = grossProfitMargin,
            totalExpenses = totalExpenses,
            netProfit = netProfit,
            totalReceivables = totalReceivables,
            totalPayables = totalPayables,
            totalProducts = totalProducts,
            totalStockUnits = totalStockUnits,
            inventoryCostValue = inventoryCostValue,
            inventoryRetailValue = inventoryRetailValue,
            lowStockCount = lowStockAlerts.size,
            periodRevenue = periodRevenue,
            periodSalesCount = periodSalesCount,
            averageTicket = averageTicket,
            todayAverageOrderValue = todayAverageOrderValue,
            trendPercentage = null,
            chartPoints = chartPoints,
            chartLabels = chartLabels,
            bestSellers = bestSellers
        )

        DashboardUiState(
            isLoading = false,
            metrics = metrics,
            lowStockAlerts = lowStockAlerts,
            recentSales = sales.take(8),
            allSales = sales,
            allProducts = products,
            allCustomers = customers,
            allSuppliers = suppliers,
            allExpenses = expenses,
            expensesByCategory = categoryExpenses,
            selectedPeriod = period,
            settings = settings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun selectPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
    }

    // Product actions
    fun addProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.insertProduct(product)
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun updateProductStock(productId: Long, newStockQuantity: Int) {
        viewModelScope.launch {
            val current = uiState.value.allProducts.find { it.id == productId }
            if (current != null) {
                repository.updateProduct(current.copy(stockQuantity = newStockQuantity))
            }
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            repository.deleteProductById(productId)
        }
    }

    fun recordSale(sale: SaleEntity, items: List<SaleItemEntity>) {
        viewModelScope.launch {
            repository.recordSaleAndDeductStock(sale, items)
        }
    }

    fun restockProduct(productId: Long, amount: Int = 10) {
        viewModelScope.launch {
            repository.restockProduct(productId, amount)
        }
    }

    // Customer actions
    fun addCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.insertCustomer(customer)
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomerById(id)
        }
    }

    fun recordCustomerPayment(payment: CustomerPaymentEntity) {
        viewModelScope.launch {
            repository.recordCustomerPayment(payment)
        }
    }

    // Supplier actions
    fun addSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            repository.insertSupplier(supplier)
        }
    }

    fun updateSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            repository.updateSupplier(supplier)
        }
    }

    fun deleteSupplier(id: Long) {
        viewModelScope.launch {
            repository.deleteSupplierById(id)
        }
    }

    fun recordSupplierPayment(payment: SupplierPaymentEntity) {
        viewModelScope.launch {
            repository.recordSupplierPayment(payment)
        }
    }

    // Expense actions
    fun addExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.insertExpense(expense)
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpenseById(id)
        }
    }

    // Settings actions
    fun updateStoreName(name: String) {
        viewModelScope.launch {
            repository.updateStoreName(name)
        }
    }

    fun updateCurrency(currency: String, code: String = "PKR", name: String = "Pakistani Rupee") {
        viewModelScope.launch {
            repository.updateCurrency(currency, code, name)
        }
    }

    fun updateBusinessCategory(category: String) {
        viewModelScope.launch {
            repository.updateBusinessCategory(category)
        }
    }

    fun updateTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            repository.updateThemeMode(themeMode)
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            repository.updateLanguage(lang)
        }
    }

    fun updateBusinessProfile(phone: String, address: String, ntn: String) {
        viewModelScope.launch {
            repository.updateBusinessProfile(phone, address, ntn)
        }
    }

    fun saveBusinessProfile(
        storeName: String,
        category: String,
        phone: String,
        address: String,
        ntn: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.updateFullBusinessProfile(
                    storeName = storeName.trim(),
                    category = category.trim(),
                    phone = phone.trim(),
                    address = address.trim(),
                    ntn = ntn.trim()
                )
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to save business profile")
            }
        }
    }

    fun updateInvoiceFooter(footer: String) {
        viewModelScope.launch {
            repository.updateInvoiceFooter(footer)
        }
    }

    fun saveInvoiceSettings(
        template: String,
        footer: String,
        showTax: Boolean,
        showPhone: Boolean,
        showAddress: Boolean,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.updateInvoiceSettings(template, footer, showTax, showPhone, showAddress)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to save invoice settings")
            }
        }
    }



    fun completeOnboarding(lang: String, storeName: String, category: String) {
        viewModelScope.launch {
            repository.updateLanguage(lang)
            if (storeName.isNotBlank()) {
                repository.updateStoreName(storeName)
            }
            repository.updateBusinessCategory(category)
            repository.updateOnboardingCompleted(true)
        }
    }

    fun resetVault() {
        viewModelScope.launch {
            repository.resetDatabaseToDefaults()
        }
    }

    private fun generateChartData(
        period: TimePeriod,
        sales: List<SaleEntity>,
        periodRevenue: Double,
        totalRevenue: Double
    ): Pair<List<Float>, List<String>> {
        return when (period) {
            TimePeriod.TODAY -> {
                val labels = listOf("8 AM", "11 AM", "2 PM", "5 PM", "8 PM", "Now")
                val base = periodRevenue.toFloat()
                val points = listOf(
                    (base * 0.10f).coerceAtLeast(80f),
                    (base * 0.25f).coerceAtLeast(240f),
                    (base * 0.45f).coerceAtLeast(510f),
                    (base * 0.65f).coerceAtLeast(820f),
                    (base * 0.85f).coerceAtLeast(1240f),
                    base.coerceAtLeast(1600f)
                )
                points to labels
            }
            TimePeriod.WEEK -> {
                val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val base = periodRevenue.toFloat()
                val points = listOf(
                    (base * 0.12f).coerceAtLeast(300f),
                    (base * 0.18f).coerceAtLeast(450f),
                    (base * 0.30f).coerceAtLeast(750f),
                    (base * 0.42f).coerceAtLeast(920f),
                    (base * 0.60f).coerceAtLeast(1400f),
                    (base * 0.82f).coerceAtLeast(2100f),
                    base.coerceAtLeast(2800f)
                )
                points to labels
            }
            TimePeriod.MONTH -> {
                val labels = listOf("W1", "W2", "W3", "W4")
                val base = periodRevenue.toFloat()
                val points = listOf(
                    (base * 0.20f).coerceAtLeast(1200f),
                    (base * 0.45f).coerceAtLeast(2600f),
                    (base * 0.75f).coerceAtLeast(4200f),
                    base.coerceAtLeast(5800f)
                )
                points to labels
            }
            TimePeriod.ALL -> {
                val labels = listOf("Q1", "Q2", "Q3", "Q4", "Current")
                val base = totalRevenue.toFloat()
                val points = listOf(
                    (base * 0.15f).coerceAtLeast(2000f),
                    (base * 0.35f).coerceAtLeast(4500f),
                    (base * 0.55f).coerceAtLeast(7000f),
                    (base * 0.80f).coerceAtLeast(9500f),
                    base.coerceAtLeast(12000f)
                )
                points to labels
            }
        }
    }
}

class DashboardViewModelFactory(
    private val repository: ShopFlowRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
