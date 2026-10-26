package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

data class CategoryExpenseSummary(
    val category: String,
    val totalAmount: Double,
    val count: Int
)

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getExpensesSince(startTime: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses")
    fun getTotalExpenses(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE timestamp >= :startTime")
    fun getExpensesSinceTotal(startTime: Long): Flow<Double>

    @Query("""
        SELECT category, SUM(amount) as totalAmount, COUNT(*) as count 
        FROM expenses 
        GROUP BY category 
        ORDER BY totalAmount DESC
    """)
    fun getExpensesByCategory(): Flow<List<CategoryExpenseSummary>>

    @Query("""
        SELECT * FROM expenses 
        WHERE title LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%' 
           OR note LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchExpenses(query: String): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()
}
