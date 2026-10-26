package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val address: String = "",
    val outstandingBalance: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customer_payments")
data class CustomerPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val amount: Double,
    val paymentMethod: String, // CASH, BANK, DIGITAL
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
