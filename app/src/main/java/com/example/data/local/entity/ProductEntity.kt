package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
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
    val imageUri: String = "", // Preset icon tag or content:// photo URI
    val isFeatured: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
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
}
