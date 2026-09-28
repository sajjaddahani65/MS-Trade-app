package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_positions")
data class TradePositionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountType: String, // "DEMO" or "REAL"
    val symbol: String,      // e.g. "BTC/USD"
    val direction: String,   // "BUY" or "SELL"
    val lotSize: Double,     // e.g. 0.1
    val openPrice: Double,
    val currentPrice: Double,
    val takeProfit: Double?,
    val stopLoss: Double?,
    val leverage: Int,
    val margin: Double,
    val openTime: Long = System.currentTimeMillis(),
    val closeTime: Long? = null,
    val closePrice: Double? = null,
    val profit: Double = 0.0,
    val status: String = "OPEN", // "OPEN", "PENDING", "CLOSED"
    val pendingType: String? = null, // "BUY_LIMIT", "SELL_LIMIT", "BUY_STOP", "SELL_STOP"
    val targetTriggerPrice: Double? = null,
    val closeReason: String? = null // "MANUAL", "TAKE_PROFIT", "STOP_LOSS", "LIQUIDATION"
)

@Entity(tableName = "account_balances")
data class AccountEntity(
    @PrimaryKey
    val accountType: String, // "DEMO" or "REAL"
    val balance: Double,
    val currency: String = "USD",
    val bdcCoins: Long = 2500,
    val vipLevel: Int = 2
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey
    val symbol: String,
    val isFavorite: Boolean = true
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountType: String,
    val type: String, // "DEPOSIT", "WITHDRAWAL", "TRADE_PROFIT", "TRADE_LOSS", "REWARD"
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String
)

@Entity(tableName = "rewards_state")
data class DailyRewardEntity(
    @PrimaryKey
    val id: Int = 1,
    val streakDays: Int = 3,
    val lastCheckInTimestamp: Long = 0,
    val isMiningActive: Boolean = false,
    val miningEndTime: Long = 0,
    val miningHashrate: String = "120 MH/s",
    val couponsCount: Int = 2
)
