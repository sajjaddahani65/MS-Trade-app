package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crypto_holdings")
data class CryptoHoldingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountType: String, // "DEMO" or "REAL"
    val symbol: String,      // e.g. "BTC", "ETH", "SOL", "BNB"
    val name: String,        // e.g. "Bitcoin"
    val amount: Double,      // quantity held, e.g. 0.42
    val avgBuyPrice: Double, // purchase price in USD
    val colorHex: String = "#F0B90B",
    val lastUpdated: Long = System.currentTimeMillis()
)
