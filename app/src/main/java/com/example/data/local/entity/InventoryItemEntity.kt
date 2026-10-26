package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing an inventory item for the business.
 * Supports full local offline persistence, stock tracking, pricing, and category management.
 */
@Entity(
    tableName = "inventory_items",
    indices = [
        Index(value = ["sku"], unique = true),
        Index(value = ["barcode"]),
        Index(value = ["category"])
    ]
)
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val sku: String,
    val barcode: String = "",
    val category: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val stockQuantity: Int,
    val minStockThreshold: Int = 5,
    val unit: String = "pcs",
    val supplierName: String = "",
    val locationRack: String = "",
    val imageUri: String = "",
    val isFeatured: Boolean = false,
    val isTaxable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val expiryDate: Long? = null,
    val batchNumber: String = ""
) {
    @get:Ignore
    val costPrice: Double
        get() = purchasePrice

    @get:Ignore
    val isLowStock: Boolean
        get() = stockQuantity <= minStockThreshold

    @get:Ignore
    val isOutOfStock: Boolean
        get() = stockQuantity <= 0

    @get:Ignore
    val profitMarginPercent: Double
        get() = if (sellingPrice > 0) ((sellingPrice - purchasePrice) / sellingPrice) * 100.0 else 0.0

    @get:Ignore
    val isExpiringSoon: Boolean
        get() = expiryDate != null && (expiryDate - System.currentTimeMillis()) in 0..(30L * 24 * 3600 * 1000)

    @get:Ignore
    val isExpired: Boolean
        get() = expiryDate != null && expiryDate < System.currentTimeMillis()

    @get:Ignore
    val totalCostValue: Double
        get() = purchasePrice * stockQuantity

    @get:Ignore
    val totalRetailValue: Double
        get() = sellingPrice * stockQuantity
}

/**
 * Type alias for clean domain naming.
 */
typealias InventoryItem = InventoryItemEntity

/**
 * Convenience extension converters between ProductEntity and InventoryItemEntity.
 */
fun ProductEntity.toInventoryItem(): InventoryItemEntity = InventoryItemEntity(
    id = id,
    name = name,
    description = description,
    sku = sku,
    barcode = barcode,
    category = category,
    purchasePrice = purchasePrice,
    sellingPrice = sellingPrice,
    stockQuantity = stockQuantity,
    minStockThreshold = minStockThreshold,
    imageUri = imageUri,
    isFeatured = isFeatured,
    createdAt = createdAt,
    updatedAt = createdAt,
    expiryDate = expiryDate,
    batchNumber = batchNumber
)

fun InventoryItemEntity.toProduct(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    description = description,
    sku = sku,
    barcode = barcode,
    category = category,
    purchasePrice = purchasePrice,
    sellingPrice = sellingPrice,
    stockQuantity = stockQuantity,
    minStockThreshold = minStockThreshold,
    imageUri = imageUri,
    isFeatured = isFeatured,
    createdAt = createdAt,
    expiryDate = expiryDate,
    batchNumber = batchNumber
)
