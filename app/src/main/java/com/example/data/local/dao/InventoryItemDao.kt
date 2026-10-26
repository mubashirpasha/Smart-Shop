package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing local offline CRUD operations on business inventory items.
 * All reactive reads return Flow<T>, and all mutations are suspend functions.
 */
@Dao
interface InventoryItemDao {

    // ==========================================
    // CREATE
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InventoryItemEntity>)

    // ==========================================
    // READ (Reactive Flow)
    // ==========================================

    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items ORDER BY createdAt DESC")
    fun getAllItemsByRecent(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    fun getItemById(id: Long): Flow<InventoryItemEntity?>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getItemByIdDirect(id: Long): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE sku = :sku LIMIT 1")
    fun getItemBySku(sku: String): Flow<InventoryItemEntity?>

    @Query("SELECT * FROM inventory_items WHERE barcode = :barcode LIMIT 1")
    fun getItemByBarcode(barcode: String): Flow<InventoryItemEntity?>

    @Query("SELECT * FROM inventory_items WHERE category = :category ORDER BY name ASC")
    fun getItemsByCategory(category: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE stockQuantity <= minStockThreshold ORDER BY stockQuantity ASC")
    fun getLowStockItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE stockQuantity <= 0 ORDER BY name ASC")
    fun getOutOfStockItems(): Flow<List<InventoryItemEntity>>

    @Query("""
        SELECT * FROM inventory_items 
        WHERE name LIKE '%' || :query || '%' 
           OR sku LIKE '%' || :query || '%' 
           OR barcode LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchItems(query: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT COUNT(*) FROM inventory_items")
    fun getItemCount(): Flow<Int>

    @Query("SELECT SUM(stockQuantity) FROM inventory_items")
    fun getTotalStockUnits(): Flow<Int?>

    @Query("SELECT SUM(purchasePrice * stockQuantity) FROM inventory_items")
    fun getTotalCostValuation(): Flow<Double?>

    @Query("SELECT SUM(sellingPrice * stockQuantity) FROM inventory_items")
    fun getTotalRetailValuation(): Flow<Double?>

    @Query("SELECT DISTINCT category FROM inventory_items ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    // ==========================================
    // UPDATE
    // ==========================================

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Update
    suspend fun updateAll(items: List<InventoryItemEntity>)

    @Query("UPDATE inventory_items SET stockQuantity = :newStock, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE inventory_items SET stockQuantity = stockQuantity + :delta, updatedAt = :updatedAt WHERE id = :id")
    suspend fun adjustStock(id: Long, delta: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE inventory_items SET purchasePrice = :cost, sellingPrice = :retail, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePricing(id: Long, cost: Double, retail: Double, updatedAt: Long = System.currentTimeMillis())

    // ==========================================
    // DELETE
    // ==========================================

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM inventory_items WHERE sku = :sku")
    suspend fun deleteItemBySku(sku: String)

    @Query("DELETE FROM inventory_items")
    suspend fun deleteAll()
}

/**
 * Type alias for clean domain naming.
 */
typealias InventoryDao = InventoryItemDao
