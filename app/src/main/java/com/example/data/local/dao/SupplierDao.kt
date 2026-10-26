package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.SupplierPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    fun getSupplierById(id: Long): Flow<SupplierEntity?>

    @Query("SELECT COALESCE(SUM(outstandingBalance), 0.0) FROM suppliers")
    fun getTotalSupplierPayables(): Flow<Double>

    @Query("""
        SELECT * FROM suppliers 
        WHERE name LIKE '%' || :query || '%' 
           OR companyName LIKE '%' || :query || '%' 
           OR phone LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchSuppliers(query: String): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(suppliers: List<SupplierEntity>)

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)

    @Query("DELETE FROM suppliers WHERE id = :id")
    suspend fun deleteSupplierById(id: Long)

    @Query("DELETE FROM suppliers")
    suspend fun clearAllSuppliers()

    // Supplier Payments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: SupplierPaymentEntity): Long

    @Query("SELECT * FROM supplier_payments WHERE supplierId = :supplierId ORDER BY timestamp DESC")
    fun getSupplierPayments(supplierId: Long): Flow<List<SupplierPaymentEntity>>

    @Transaction
    suspend fun recordSupplierPayment(payment: SupplierPaymentEntity) {
        insertPayment(payment)
        // Deduct payment amount from outstanding payables
        deductSupplierBalance(payment.supplierId, payment.amount)
    }

    @Query("UPDATE suppliers SET outstandingBalance = MAX(0.0, outstandingBalance - :amount) WHERE id = :supplierId")
    suspend fun deductSupplierBalance(supplierId: Long, amount: Double)

    @Query("UPDATE suppliers SET outstandingBalance = outstandingBalance + :amount, totalPurchased = totalPurchased + :amount WHERE id = :supplierId")
    suspend fun addSupplierPurchase(supplierId: Long, amount: Double)
}
