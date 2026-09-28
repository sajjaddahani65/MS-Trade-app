package com.example.data.model

enum class SignalDirection {
    BUY, SELL
}

data class TradingSignal(
    val id: String,
    val symbol: String,
    val direction: SignalDirection,
    val entryPrice: Double,
    val targetPrice1: Double,
    val targetPrice2: Double,
    val stopLoss: Double,
    val confidence: Int, // e.g. 92%
    val timeAgo: String,
    val rationale: String,
    val author: String = "BTC Dana Senior Analyst",
    val status: String = "ACTIVE" // ACTIVE, TARGET_REACHED, EXPIRED
)

data class EconomicEvent(
    val id: String,
    val time: String,
    val currency: String,
    val countryFlag: String,
    val title: String,
    val impact: ImpactLevel, // HIGH, MEDIUM, LOW
    val actual: String?,
    val forecast: String,
    val previous: String
)

enum class ImpactLevel {
    HIGH, MEDIUM, LOW
}

data class MarketNews(
    val id: String,
    val title: String,
    val source: String,
    val timeAgo: String,
    val category: String,
    val summary: String,
    val readMinutes: Int = 3
)
