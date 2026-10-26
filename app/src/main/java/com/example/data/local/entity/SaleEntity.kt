package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val discountPercent: Double = 0.0,
    val taxRate: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double,
    val itemCount: Int,
    val paymentMethod: String, // CASH, CARD, CREDIT, DIGITAL
    val cashTendered: Double = 0.0,
    val changeDue: Double = 0.0,
    val customerName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val customerNote: String = ""
)
