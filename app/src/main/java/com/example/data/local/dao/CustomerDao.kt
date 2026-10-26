package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.CustomerPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerById(id: Long): Flow<CustomerEntity?>

    @Query("SELECT COALESCE(SUM(outstandingBalance), 0.0) FROM customers")
    fun getTotalCustomerReceivables(): Flow<Double>

    @Query("""
        SELECT * FROM customers 
        WHERE name LIKE '%' || :query || '%' 
           OR phone LIKE '%' || :query || '%' 
           OR email LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Long)

    @Query("DELETE FROM customers")
    suspend fun clearAllCustomers()

    // Payments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: CustomerPaymentEntity): Long

    @Query("SELECT * FROM customer_payments WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getCustomerPayments(customerId: Long): Flow<List<CustomerPaymentEntity>>

    @Query("SELECT * FROM customer_payments ORDER BY timestamp DESC")
    fun getAllCustomerPayments(): Flow<List<CustomerPaymentEntity>>

    @Transaction
    suspend fun recordCustomerPayment(payment: CustomerPaymentEntity) {
        insertPayment(payment)
        // Deduct payment amount from customer's outstanding balance
        deductCustomerBalance(payment.customerId, payment.amount)
    }

    @Query("UPDATE customers SET outstandingBalance = MAX(0.0, outstandingBalance - :amount) WHERE id = :customerId")
    suspend fun deductCustomerBalance(customerId: Long, amount: Double)

    @Query("UPDATE customers SET outstandingBalance = outstandingBalance + :amount, totalPurchases = totalPurchases + :amount WHERE id = :customerId")
    suspend fun addCustomerCreditPurchase(customerId: Long, amount: Double)
}
