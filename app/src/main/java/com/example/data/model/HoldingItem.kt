package com.example.data.model

data class HoldingItem(
    val id: Long,
    val symbol: String,
    val name: String,
    val amount: Double,
    val avgBuyPrice: Double,
    val currentPrice: Double,
    val currentValue: Double,
    val costBasis: Double,
    val totalPnL: Double,
    val totalPnLPercent: Double,
    val change24h: Double,
    val valueChange24h: Double,
    val allocationPercent: Double,
    val colorHex: String
)
