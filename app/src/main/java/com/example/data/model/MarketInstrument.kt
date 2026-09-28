package com.example.data.model

enum class InstrumentCategory(val displayName: String) {
    ALL("All"),
    CRYPTO("Crypto"),
    FOREX("Forex"),
    COMMODITIES("Metals & Oil"),
    INDICES("Indices")
}

data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 100.0
)

data class MarketInstrument(
    val symbol: String,
    val name: String,
    val category: InstrumentCategory,
    val price: Double,
    val prevPrice: Double,
    val change24h: Double,
    val high24h: Double,
    val low24h: Double,
    val volume24h: String,
    val spread: Double,
    val decimals: Int = 2,
    val contractSize: Double = 1.0,
    val defaultLeverage: Int = 100,
    val leverageOptions: List<Int> = listOf(20, 50, 100, 200),
    val candles: List<Candle> = emptyList(),
    val isFavorite: Boolean = false
) {
    val isUp: Boolean get() = price >= prevPrice
    val changePercent: Double get() = change24h
    fun formatPrice(value: Double = price): String {
        return "%,.${decimals}f".format(value)
    }
}
