package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stockQuantity <= minStockThreshold ORDER BY stockQuantity ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    fun getProductCount(): Flow<Int>

    @Query("SELECT SUM(stockQuantity) FROM products")
    fun getTotalStockUnits(): Flow<Int?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    fun getProductByBarcode(barcode: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE isFeatured = 1 ORDER BY name ASC")
    fun getFeaturedProducts(): Flow<List<ProductEntity>>

    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("""
        SELECT * FROM products 
        WHERE name LIKE '%' || :query || '%' 
           OR sku LIKE '%' || :query || '%' 
           OR barcode LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("DELETE FROM products")
    suspend fun clearAllProducts()

    @Query("UPDATE products SET stockQuantity = :newStockQuantity WHERE id = :id")
    suspend fun updateStock(id: Long, newStockQuantity: Int)

    @Query("UPDATE products SET stockQuantity = stockQuantity + :amount WHERE id = :id")
    suspend fun restock(id: Long, amount: Int)
}
