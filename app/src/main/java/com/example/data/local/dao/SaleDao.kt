package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSales(limit: Int): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getSalesSince(startTime: Long): Flow<List<SaleEntity>>

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM sales")
    fun getTotalRevenue(): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM sales WHERE timestamp >= :todayStart")
    fun getTodayRevenue(todayStart: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM sales")
    fun getSaleCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sales WHERE timestamp >= :todayStart")
    fun getTodaySaleCount(todayStart: Long): Flow<Int>

    @Query("SELECT * FROM sales WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getSaleByInvoice(invoiceNumber: String): SaleEntity?

    @Query("""
        SELECT * FROM sales 
        WHERE invoiceNumber LIKE '%' || :query || '%' 
           OR customerName LIKE '%' || :query || '%'
           OR customerNote LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchSales(query: String): Flow<List<SaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getSaleItems(saleId: Long): Flow<List<SaleItemEntity>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItemsList(saleId: Long): List<SaleItemEntity>

    @Query("SELECT * FROM sale_items")
    fun getAllSaleItems(): Flow<List<SaleItemEntity>>

    @Query("DELETE FROM sales")
    suspend fun clearAllSales()

    @Query("UPDATE products SET stockQuantity = stockQuantity - :quantity WHERE id = :productId AND stockQuantity >= :quantity")
    suspend fun deductStockIfAvailable(productId: Long, quantity: Int): Int

    @Transaction
    suspend fun recordSaleWithItems(sale: SaleEntity, items: List<SaleItemEntity>): Long {
        val saleId = insertSale(sale)
        val itemsWithSaleId = items.map { it.copy(saleId = saleId) }
        insertSaleItems(itemsWithSaleId)
        return saleId
    }

    @Transaction
    suspend fun recordSaleWithAtomicStockDeduction(
        sale: SaleEntity,
        items: List<SaleItemEntity>
    ): Long {
        for (item in items) {
            val rowsUpdated = deductStockIfAvailable(item.productId, item.quantity)
            if (rowsUpdated == 0) {
                throw IllegalStateException("Insufficient inventory in vault for: ${item.productName}")
            }
        }
        val saleId = insertSale(sale)
        val itemsWithSaleId = items.map { it.copy(saleId = saleId) }
        insertSaleItems(itemsWithSaleId)
        return saleId
    }
}
