package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val barcode: String,
    val name: String,
    val category: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val stockQuantity: Int,
    val lowStockThreshold: Int = 5
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val address: String = "",
    val totalUdhaar: Double = 0.0
)

@Entity(tableName = "transactions")
data class LedgerTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int?, // Null means cash sale / walk-in POS
    val type: String, // "CASH_REVENUE", "UDHAAR_DEBIT" (credit purchase), "UDHAAR_CREDIT" (received payment)
    val amount: Double,
    val costOfGoods: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val itemsSummary: String = "",
    val note: String = ""
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // e.g., Rent, Electricity, Stock, salary, Others
    val description: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
)
