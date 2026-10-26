package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String, // Rent, Utilities, Salaries, Logistics, Marketing, Maintenance, General
    val paymentMethod: String = "CASH",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
