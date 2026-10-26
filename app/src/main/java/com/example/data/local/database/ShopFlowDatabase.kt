package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.InventoryItemDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SaleDao
import com.example.data.local.dao.SupplierDao
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.CustomerPaymentEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.SupplierPaymentEntity
import com.example.data.local.entity.toInventoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        InventoryItemEntity::class,
        ProductEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        CustomerEntity::class,
        CustomerPaymentEntity::class,
        SupplierEntity::class,
        SupplierPaymentEntity::class,
        ExpenseEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class ShopFlowDatabase : RoomDatabase() {

    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: ShopFlowDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ShopFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShopFlowDatabase::class.java,
                    "shopflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(
                            database.inventoryItemDao(),
                            database.productDao(),
                            database.saleDao(),
                            database.customerDao(),
                            database.supplierDao(),
                            database.expenseDao()
                        )
                    }
                }
            }
        }

        suspend fun populateInitialData(
            inventoryItemDao: InventoryItemDao? = null,
            productDao: ProductDao,
            saleDao: SaleDao,
            customerDao: CustomerDao,
            supplierDao: SupplierDao,
            expenseDao: ExpenseDao
        ) {
            val currentTime = System.currentTimeMillis()
            val oneHour = 3600_000L
            val oneDay = 86400_000L

            // 1. Seed Products
            val seedProducts = listOf(
                ProductEntity(
                    id = 1,
                    name = "CyberCore Quantum Workstation",
                    description = "Enterprise-grade quantum compute module with cryogenic heatpipe cooling.",
                    sku = "SF-CC900",
                    barcode = "880923145001",
                    category = "Hardware",
                    purchasePrice = 120000.0,
                    sellingPrice = 189000.0,
                    stockQuantity = 8,
                    minStockThreshold = 4,
                    imageUri = "preset_workstation",
                    isFeatured = true,
                    createdAt = currentTime - (7 * oneDay)
                ),
                ProductEntity(
                    id = 2,
                    name = "HoloLens Vision HUD Pro",
                    description = "Ultra-lightweight augmented reality eyepiece with 8K retina projection.",
                    sku = "SF-HL200",
                    barcode = "880923145002",
                    category = "Wearables",
                    purchasePrice = 38000.0,
                    sellingPrice = 59900.0,
                    stockQuantity = 14,
                    minStockThreshold = 5,
                    imageUri = "preset_hud",
                    isFeatured = true,
                    createdAt = currentTime - (6 * oneDay)
                ),
                ProductEntity(
                    id = 3,
                    name = "NeuroPulse Bio-Controller",
                    description = "Sub-millisecond tactile haptic interface with neural impulse tracking.",
                    sku = "SF-NP450",
                    barcode = "880923145003",
                    category = "Peripherals",
                    purchasePrice = 8500.0,
                    sellingPrice = 14900.0,
                    stockQuantity = 2,
                    minStockThreshold = 6,
                    imageUri = "preset_controller",
                    isFeatured = false,
                    createdAt = currentTime - (5 * oneDay),
                    batchNumber = "LOT-NP26",
                    expiryDate = currentTime + (12 * oneDay) // Expiring soon in 12 days
                ),
                ProductEntity(
                    id = 4,
                    name = "AeroMatrix Cooling Core",
                    description = "Dual-stage vacuum liquid vapor heat exchanger.",
                    sku = "SF-AM110",
                    barcode = "880923145004",
                    category = "Accessories",
                    purchasePrice = 2800.0,
                    sellingPrice = 6400.0,
                    stockQuantity = 28,
                    minStockThreshold = 8,
                    imageUri = "preset_cooling",
                    isFeatured = false,
                    createdAt = currentTime - (4 * oneDay),
                    batchNumber = "LOT-AM26",
                    expiryDate = currentTime + (90 * oneDay)
                ),
                ProductEntity(
                    id = 5,
                    name = "FluxGlow Neon Strip 5M",
                    description = "Individually addressable magnetic photonic strip.",
                    sku = "SF-FG080",
                    barcode = "880923145005",
                    category = "Ambient",
                    purchasePrice = 1400.0,
                    sellingPrice = 3490.0,
                    stockQuantity = 3,
                    minStockThreshold = 10,
                    imageUri = "preset_neon",
                    isFeatured = false,
                    createdAt = currentTime - (3 * oneDay),
                    batchNumber = "LOT-FG25",
                    expiryDate = currentTime - (3 * oneDay) // Expired 3 days ago
                ),
                ProductEntity(
                    id = 6,
                    name = "SpectraFlow Ultra Display 4K",
                    description = "Bezel-less micro-OLED panel with 240Hz refresh rate.",
                    sku = "SF-SF700",
                    barcode = "880923145006",
                    category = "Displays",
                    purchasePrice = 42000.0,
                    sellingPrice = 74900.0,
                    stockQuantity = 6,
                    minStockThreshold = 3,
                    imageUri = "preset_display",
                    isFeatured = true,
                    createdAt = currentTime - (2 * oneDay)
                ),
                ProductEntity(
                    id = 7,
                    name = "Titanium Hex Keyring EDC",
                    description = "Grade 5 titanium alloy multi-tool containing magnetic bits.",
                    sku = "SF-TH010",
                    barcode = "880923145007",
                    category = "Gear",
                    purchasePrice = 850.0,
                    sellingPrice = 2200.0,
                    stockQuantity = 45,
                    minStockThreshold = 12,
                    imageUri = "preset_titanium",
                    isFeatured = false,
                    createdAt = currentTime - oneDay
                )
            )
            productDao.insertAll(seedProducts)
            inventoryItemDao?.insertAll(seedProducts.map { it.toInventoryItem() })

            // 2. Seed Sales
            val sale1 = SaleEntity(
                invoiceNumber = "INV-2026-081",
                subtotal = 189000.0,
                discountAmount = 0.0,
                taxRate = 0.0,
                totalAmount = 189000.0,
                itemCount = 1,
                paymentMethod = "DIGITAL",
                customerName = "Bilal Ahmed",
                timestamp = currentTime - (1 * oneHour)
            )
            val items1 = listOf(
                SaleItemEntity(
                    productId = 1,
                    productName = "CyberCore Quantum Workstation",
                    quantity = 1,
                    unitPrice = 189000.0,
                    costPrice = 120000.0,
                    subtotal = 189000.0
                )
            )
            saleDao.recordSaleWithItems(sale1, items1)

            val sale2 = SaleEntity(
                invoiceNumber = "INV-2026-080",
                subtotal = 66300.0,
                discountAmount = 0.0,
                taxRate = 0.0,
                totalAmount = 66300.0,
                itemCount = 2,
                paymentMethod = "CARD",
                customerName = "Zainab Malik",
                timestamp = currentTime - (3 * oneHour)
            )
            val items2 = listOf(
                SaleItemEntity(
                    productId = 2,
                    productName = "HoloLens Vision HUD Pro",
                    quantity = 1,
                    unitPrice = 59900.0,
                    costPrice = 38000.0,
                    subtotal = 59900.0
                ),
                SaleItemEntity(
                    productId = 4,
                    productName = "AeroMatrix Cooling Core",
                    quantity = 1,
                    unitPrice = 6400.0,
                    costPrice = 2800.0,
                    subtotal = 6400.0
                )
            )
            saleDao.recordSaleWithItems(sale2, items2)

            val sale3 = SaleEntity(
                invoiceNumber = "INV-2026-079",
                subtotal = 20590.0,
                discountAmount = 0.0,
                taxRate = 0.0,
                totalAmount = 20590.0,
                itemCount = 3,
                paymentMethod = "CASH",
                cashTendered = 25000.0,
                changeDue = 4410.0,
                customerName = "Usman Tariq",
                timestamp = currentTime - (18 * oneHour)
            )
            val items3 = listOf(
                SaleItemEntity(
                    productId = 3,
                    productName = "NeuroPulse Bio-Controller",
                    quantity = 1,
                    unitPrice = 14900.0,
                    costPrice = 8500.0,
                    subtotal = 14900.0
                ),
                SaleItemEntity(
                    productId = 5,
                    productName = "FluxGlow Neon Strip 5M",
                    quantity = 1,
                    unitPrice = 3490.0,
                    costPrice = 1400.0,
                    subtotal = 3490.0
                ),
                SaleItemEntity(
                    productId = 7,
                    productName = "Titanium Hex Keyring EDC",
                    quantity = 1,
                    unitPrice = 2200.0,
                    costPrice = 850.0,
                    subtotal = 2200.0
                )
            )
            saleDao.recordSaleWithItems(sale3, items3)

            // 3. Seed Customers
            val seedCustomers = listOf(
                CustomerEntity(
                    id = 1,
                    name = "Bilal Ahmed",
                    phone = "+92 300 9876543",
                    email = "bilal.ahmed@example.com",
                    address = "Sector F-7/2, Islamabad",
                    outstandingBalance = 0.0,
                    totalPurchases = 189000.0,
                    createdAt = currentTime - (14 * oneDay)
                ),
                CustomerEntity(
                    id = 2,
                    name = "Zainab Malik",
                    phone = "+92 321 5551234",
                    email = "zainab.malik@outlook.com",
                    address = "DHA Phase 5, Lahore",
                    outstandingBalance = 15000.0,
                    totalPurchases = 81300.0,
                    createdAt = currentTime - (10 * oneDay)
                ),
                CustomerEntity(
                    id = 3,
                    name = "Usman Tariq",
                    phone = "+92 333 4448899",
                    email = "usman.tariq@gmail.com",
                    address = "Clifton Block 4, Karachi",
                    outstandingBalance = 0.0,
                    totalPurchases = 20590.0,
                    createdAt = currentTime - (5 * oneDay)
                ),
                CustomerEntity(
                    id = 4,
                    name = "Apex Cyber Solutions",
                    phone = "+92 51 2289900",
                    email = "procurement@apexcyber.pk",
                    address = "I-9 Industrial Area, Islamabad",
                    outstandingBalance = 45000.0,
                    totalPurchases = 240000.0,
                    createdAt = currentTime - (20 * oneDay)
                )
            )
            customerDao.insertAll(seedCustomers)

            // 4. Seed Customer Payments
            customerDao.insertPayment(
                CustomerPaymentEntity(
                    customerId = 2,
                    amount = 20000.0,
                    paymentMethod = "BANK",
                    note = "Bank Transfer Ref #99014",
                    timestamp = currentTime - (2 * oneDay)
                )
            )

            // 5. Seed Suppliers
            val seedSuppliers = listOf(
                SupplierEntity(
                    id = 1,
                    name = "Tariq Mehmood",
                    companyName = "Quantum Micro Systems PK",
                    phone = "+92 42 35789012",
                    email = "sales@quantum-micro.pk",
                    address = "Hafeez Centre, Gulberg III, Lahore",
                    outstandingBalance = 60000.0,
                    totalPurchased = 480000.0,
                    createdAt = currentTime - (30 * oneDay)
                ),
                SupplierEntity(
                    id = 2,
                    name = "Kamran Siddiqui",
                    companyName = "Apex Optics & Displays Ltd",
                    phone = "+92 21 34567890",
                    email = "kamran@apexoptics.com",
                    address = "Techno City Mall, I.I. Chundrigar Rd, Karachi",
                    outstandingBalance = 25000.0,
                    totalPurchased = 210000.0,
                    createdAt = currentTime - (25 * oneDay)
                ),
                SupplierEntity(
                    id = 3,
                    name = "Shenzhen Nova Logistics",
                    companyName = "Nova Global Components",
                    phone = "+86 755 889900",
                    email = "orders@novachina.hk",
                    address = "Futian District, Shenzhen, China",
                    outstandingBalance = 0.0,
                    totalPurchased = 150000.0,
                    createdAt = currentTime - (45 * oneDay)
                )
            )
            supplierDao.insertAll(seedSuppliers)

            // 6. Seed Supplier Payments
            supplierDao.insertPayment(
                SupplierPaymentEntity(
                    supplierId = 1,
                    amount = 100000.0,
                    paymentMethod = "BANK",
                    note = "Invoice PO-2026-102 settlement",
                    timestamp = currentTime - (4 * oneDay)
                )
            )

            // 7. Seed Business Expenses
            val seedExpenses = listOf(
                ExpenseEntity(
                    title = "Commercial Shop Monthly Rent",
                    amount = 65000.0,
                    category = "Rent",
                    paymentMethod = "BANK",
                    note = "October 2026 rent payment",
                    timestamp = currentTime - (3 * oneDay)
                ),
                ExpenseEntity(
                    title = "Commercial Electricity & Generator Fuel",
                    amount = 18500.0,
                    category = "Utilities",
                    paymentMethod = "CASH",
                    note = "IESCO electricity bill & backup diesel",
                    timestamp = currentTime - (2 * oneDay)
                ),
                ExpenseEntity(
                    title = "Staff Salaries - Operations Team",
                    amount = 75000.0,
                    category = "Salaries",
                    paymentMethod = "BANK",
                    note = "Store manager and cashier wages",
                    timestamp = currentTime - (1 * oneDay)
                ),
                ExpenseEntity(
                    title = "Courier & Customer Delivery Express",
                    amount = 4200.0,
                    category = "Logistics",
                    paymentMethod = "CASH",
                    note = "TCS / Leopards courier parcel deliveries",
                    timestamp = currentTime - (12 * oneHour)
                ),
                ExpenseEntity(
                    title = "Store High-Speed Fiber Internet",
                    amount = 3500.0,
                    category = "Utilities",
                    paymentMethod = "DIGITAL",
                    note = "Nayatel 100Mbps business line",
                    timestamp = currentTime - (5 * oneDay)
                )
            )
            expenseDao.insertAll(seedExpenses)
        }
    }
}
