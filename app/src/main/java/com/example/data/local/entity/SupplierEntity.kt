package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val companyName: String = "",
    val phone: String,
    val email: String = "",
    val address: String = "",
    val outstandingBalance: Double = 0.0,
    val totalPurchased: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "supplier_payments")
data class SupplierPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supplierId: Long,
    val amount: Double,
    val paymentMethod: String, // CASH, BANK, DIGITAL
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
