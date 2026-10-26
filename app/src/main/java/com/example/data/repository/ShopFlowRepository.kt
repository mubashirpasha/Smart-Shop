package com.example.data.repository

import com.example.data.local.dao.CategoryExpenseSummary
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.InventoryItemDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SaleDao
import com.example.data.local.dao.SupplierDao
import com.example.data.local.database.ShopFlowDatabase
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.CustomerPaymentEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.SupplierPaymentEntity
import com.example.data.local.preferences.AppSettings
import com.example.data.local.preferences.ShopFlowPreferences
import com.example.data.local.preferences.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ShopFlowRepository(
    private val productDao: ProductDao,
    private val saleDao: SaleDao,
    private val customerDao: CustomerDao,
    private val supplierDao: SupplierDao,
    private val expenseDao: ExpenseDao,
    private val preferences: ShopFlowPreferences,
    private val inventoryItemDao: InventoryItemDao? = null
) {
    // Inventory Items (Local offline CRUD operations)
    val allInventoryItems: Flow<List<InventoryItemEntity>>? = inventoryItemDao?.getAllItems()
    val lowStockInventoryItems: Flow<List<InventoryItemEntity>>? = inventoryItemDao?.getLowStockItems()
    val outOfStockInventoryItems: Flow<List<InventoryItemEntity>>? = inventoryItemDao?.getOutOfStockItems()
    val inventoryItemCount: Flow<Int>? = inventoryItemDao?.getItemCount()
    val totalInventoryStockUnits: Flow<Int?>? = inventoryItemDao?.getTotalStockUnits()
    val totalInventoryCostValuation: Flow<Double?>? = inventoryItemDao?.getTotalCostValuation()
    val totalInventoryRetailValuation: Flow<Double?>? = inventoryItemDao?.getTotalRetailValuation()
    val allInventoryCategories: Flow<List<String>>? = inventoryItemDao?.getAllCategories()

    fun getInventoryItemById(id: Long): Flow<InventoryItemEntity?>? = inventoryItemDao?.getItemById(id)
    suspend fun getInventoryItemByIdDirect(id: Long): InventoryItemEntity? = inventoryItemDao?.getItemByIdDirect(id)
    fun getInventoryItemBySku(sku: String): Flow<InventoryItemEntity?>? = inventoryItemDao?.getItemBySku(sku)
    fun getInventoryItemByBarcode(barcode: String): Flow<InventoryItemEntity?>? = inventoryItemDao?.getItemByBarcode(barcode)
    fun searchInventoryItems(query: String): Flow<List<InventoryItemEntity>>? = inventoryItemDao?.searchItems(query)
    fun getInventoryItemsByCategory(category: String): Flow<List<InventoryItemEntity>>? = inventoryItemDao?.getItemsByCategory(category)

    suspend fun insertInventoryItem(item: InventoryItemEntity): Long = inventoryItemDao?.insertItem(item) ?: -1L
    suspend fun insertAllInventoryItems(items: List<InventoryItemEntity>) { inventoryItemDao?.insertAll(items) }
    suspend fun updateInventoryItem(item: InventoryItemEntity) { inventoryItemDao?.updateItem(item) }
    suspend fun updateInventoryStock(id: Long, newStock: Int) { inventoryItemDao?.updateStock(id, newStock) }
    suspend fun adjustInventoryStock(id: Long, delta: Int) { inventoryItemDao?.adjustStock(id, delta) }
    suspend fun updateInventoryPricing(id: Long, cost: Double, retail: Double) { inventoryItemDao?.updatePricing(id, cost, retail) }
    suspend fun deleteInventoryItem(item: InventoryItemEntity) { inventoryItemDao?.deleteItem(item) }
    suspend fun deleteInventoryItemById(id: Long) { inventoryItemDao?.deleteItemById(id) }
    suspend fun deleteInventoryItemBySku(sku: String) { inventoryItemDao?.deleteItemBySku(sku) }
    suspend fun deleteAllInventoryItems() { inventoryItemDao?.deleteAll() }

    // Products
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    val productCount: Flow<Int> = productDao.getProductCount()
    val totalStockUnits: Flow<Int?> = productDao.getTotalStockUnits()

    suspend fun insertProduct(product: ProductEntity): Long = productDao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)
    suspend fun deleteProductById(id: Long) = productDao.deleteProductById(id)
    suspend fun restockProduct(id: Long, amount: Int) = productDao.restock(id, amount)

    // Sales
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val allSaleItems: Flow<List<SaleItemEntity>> = saleDao.getAllSaleItems()
    val totalRevenue: Flow<Double> = saleDao.getTotalRevenue()
    val totalSalesCount: Flow<Int> = saleDao.getSaleCount()

    fun getRecentSales(limit: Int = 10): Flow<List<SaleEntity>> = saleDao.getRecentSales(limit)
    fun getTodayRevenue(todayStart: Long): Flow<Double> = saleDao.getTodayRevenue(todayStart)
    fun getTodaySaleCount(todayStart: Long): Flow<Int> = saleDao.getTodaySaleCount(todayStart)
    fun getSalesSince(startTime: Long): Flow<List<SaleEntity>> = saleDao.getSalesSince(startTime)
    fun getSaleItems(saleId: Long): Flow<List<SaleItemEntity>> = saleDao.getSaleItems(saleId)
    suspend fun getSaleItemsList(saleId: Long): List<SaleItemEntity> = saleDao.getSaleItemsList(saleId)
    suspend fun getSaleByInvoice(invoice: String): SaleEntity? = saleDao.getSaleByInvoice(invoice)
    fun searchSales(query: String): Flow<List<SaleEntity>> = saleDao.searchSales(query)

    suspend fun recordSale(sale: SaleEntity): Long = saleDao.insertSale(sale)

    suspend fun recordSaleAndDeductStock(
        sale: SaleEntity,
        items: List<SaleItemEntity>
    ): Long {
        return saleDao.recordSaleWithAtomicStockDeduction(sale, items)
    }

    // Customers
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val totalCustomerReceivables: Flow<Double> = customerDao.getTotalCustomerReceivables()
    fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)
    fun getCustomerById(id: Long): Flow<CustomerEntity?> = customerDao.getCustomerById(id)
    suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomerById(id: Long) = customerDao.deleteCustomerById(id)
    fun getCustomerPayments(customerId: Long): Flow<List<CustomerPaymentEntity>> = customerDao.getCustomerPayments(customerId)
    suspend fun recordCustomerPayment(payment: CustomerPaymentEntity) = customerDao.recordCustomerPayment(payment)

    // Suppliers
    val allSuppliers: Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()
    val totalSupplierPayables: Flow<Double> = supplierDao.getTotalSupplierPayables()
    fun searchSuppliers(query: String): Flow<List<SupplierEntity>> = supplierDao.searchSuppliers(query)
    fun getSupplierById(id: Long): Flow<SupplierEntity?> = supplierDao.getSupplierById(id)
    suspend fun insertSupplier(supplier: SupplierEntity): Long = supplierDao.insertSupplier(supplier)
    suspend fun updateSupplier(supplier: SupplierEntity) = supplierDao.updateSupplier(supplier)
    suspend fun deleteSupplierById(id: Long) = supplierDao.deleteSupplierById(id)
    fun getSupplierPayments(supplierId: Long): Flow<List<SupplierPaymentEntity>> = supplierDao.getSupplierPayments(supplierId)
    suspend fun recordSupplierPayment(payment: SupplierPaymentEntity) = supplierDao.recordSupplierPayment(payment)

    // Expenses
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val totalExpenses: Flow<Double> = expenseDao.getTotalExpenses()
    val expensesByCategory: Flow<List<CategoryExpenseSummary>> = expenseDao.getExpensesByCategory()
    fun getExpensesSince(startTime: Long): Flow<List<ExpenseEntity>> = expenseDao.getExpensesSince(startTime)
    fun getExpensesSinceTotal(startTime: Long): Flow<Double> = expenseDao.getExpensesSinceTotal(startTime)
    fun searchExpenses(query: String): Flow<List<ExpenseEntity>> = expenseDao.searchExpenses(query)
    suspend fun insertExpense(expense: ExpenseEntity): Long = expenseDao.insertExpense(expense)
    suspend fun deleteExpenseById(id: Long) = expenseDao.deleteExpenseById(id)

    // Preferences & Settings
    val appSettings: Flow<AppSettings> = preferences.appSettingsFlow

    suspend fun updateStoreName(name: String) = preferences.updateStoreName(name)
    suspend fun updateCurrencySymbol(symbol: String) = preferences.updateCurrencySymbol(symbol)
    suspend fun updateCurrency(symbol: String, code: String = "PKR", name: String = "Pakistani Rupee") = preferences.updateCurrencySymbol(symbol, code, name)
    suspend fun updateThemeMode(mode: ThemeMode) = preferences.updateThemeMode(mode)
    suspend fun updateLanguage(lang: String) = preferences.updateLanguage(lang)
    suspend fun updateBusinessProfile(phone: String, address: String, ntn: String) = preferences.updateBusinessProfile(phone, address, ntn)
    suspend fun updateFullBusinessProfile(
        storeName: String,
        category: String,
        phone: String,
        address: String,
        ntn: String
    ) = preferences.updateFullBusinessProfile(storeName, category, phone, address, ntn)
    suspend fun updateInvoiceFooter(footer: String) = preferences.updateInvoiceFooter(footer)
    suspend fun updateInvoiceSettings(
        template: String,
        footer: String,
        showTax: Boolean = true,
        showPhone: Boolean = true,
        showAddress: Boolean = true
    ) = preferences.updateInvoiceSettings(template, footer, showTax, showPhone, showAddress)

    suspend fun updateOnboardingCompleted(completed: Boolean) = preferences.updateOnboardingCompleted(completed)
    suspend fun updateBusinessCategory(category: String) = preferences.updateBusinessCategory(category)
    suspend fun updateLowStockThreshold(threshold: Int) = preferences.updateLowStockThreshold(threshold)

    suspend fun resetDatabaseToDefaults() {
        productDao.clearAllProducts()
        saleDao.clearAllSales()
        customerDao.clearAllCustomers()
        supplierDao.clearAllSuppliers()
        expenseDao.clearAllExpenses()
        inventoryItemDao?.deleteAll()
        ShopFlowDatabase.populateInitialData(
            inventoryItemDao = inventoryItemDao,
            productDao = productDao,
            saleDao = saleDao,
            customerDao = customerDao,
            supplierDao = supplierDao,
            expenseDao = expenseDao
        )
    }
}
