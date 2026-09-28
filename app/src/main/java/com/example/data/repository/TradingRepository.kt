package com.example.data.repository

import com.example.data.local.AccountEntity
import com.example.data.local.CryptoHoldingEntity
import com.example.data.local.DailyRewardEntity
import com.example.data.local.DanaDao
import com.example.data.local.TradePositionEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.Candle
import com.example.data.model.EconomicEvent
import com.example.data.model.ImpactLevel
import com.example.data.model.InstrumentCategory
import com.example.data.model.MarketInstrument
import com.example.data.model.MarketNews
import com.example.data.model.SignalDirection
import com.example.data.model.TradingSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class TradingRepository(
    private val dao: DanaDao,
    private val appScope: CoroutineScope
) {
    private val _instruments = MutableStateFlow<List<MarketInstrument>>(createInitialInstruments())
    val instruments: StateFlow<List<MarketInstrument>> = _instruments.asStateFlow()

    private val _signals = MutableStateFlow<List<TradingSignal>>(createInitialSignals())
    val signals: StateFlow<List<TradingSignal>> = _signals.asStateFlow()

    private val _calendarEvents = MutableStateFlow<List<EconomicEvent>>(createInitialCalendarEvents())
    val calendarEvents: StateFlow<List<EconomicEvent>> = _calendarEvents.asStateFlow()

    private val _news = MutableStateFlow<List<MarketNews>>(createInitialNews())
    val news: StateFlow<List<MarketNews>> = _news.asStateFlow()

    val watchlist: Flow<List<WatchlistEntity>> = dao.getWatchlist()

    init {
        startLivePriceSimulation()
    }

    fun getPositions(accountType: String, status: String): Flow<List<TradePositionEntity>> {
        return dao.getPositionsByStatus(accountType, status)
    }

    fun getAccount(accountType: String): Flow<AccountEntity?> {
        return dao.getAccount(accountType)
    }

    fun getTransactions(accountType: String): Flow<List<TransactionEntity>> {
        return dao.getTransactions(accountType)
    }

    fun getRewardState(): Flow<DailyRewardEntity?> {
        return dao.getRewardState()
    }

    fun getHoldings(accountType: String): Flow<List<CryptoHoldingEntity>> {
        return dao.getHoldings(accountType)
    }

    suspend fun addOrUpdateHolding(
        accountType: String,
        symbol: String,
        name: String,
        amount: Double,
        buyPrice: Double,
        colorHex: String
    ): Long {
        val existing = dao.getHoldingBySymbol(accountType, symbol)
        val entity = if (existing != null) {
            val totalAmount = existing.amount + amount
            val totalCost = (existing.amount * existing.avgBuyPrice) + (amount * buyPrice)
            val newAvg = if (totalAmount > 0) totalCost / totalAmount else buyPrice
            existing.copy(
                amount = totalAmount,
                avgBuyPrice = newAvg,
                lastUpdated = System.currentTimeMillis()
            )
        } else {
            CryptoHoldingEntity(
                accountType = accountType,
                symbol = symbol,
                name = name,
                amount = amount,
                avgBuyPrice = buyPrice,
                colorHex = colorHex,
                lastUpdated = System.currentTimeMillis()
            )
        }
        return dao.insertOrUpdateHolding(entity)
    }

    suspend fun deleteHolding(id: Long) {
        dao.deleteHoldingById(id)
    }

    suspend fun toggleWatchlist(symbol: String, isFav: Boolean) {
        if (isFav) {
            dao.deleteWatchlist(symbol)
        } else {
            dao.insertWatchlist(WatchlistEntity(symbol = symbol, isFavorite = true))
        }
    }

    suspend fun openPosition(
        accountType: String,
        symbol: String,
        direction: String,
        lotSize: Double,
        leverage: Int,
        takeProfit: Double?,
        stopLoss: Double?
    ): Result<Long> {
        val instrument = _instruments.value.find { it.symbol == symbol }
            ?: return Result.failure(Exception("Instrument $symbol not found"))

        val currentPrice = instrument.price
        val contractSize = instrument.contractSize
        val margin = (currentPrice * lotSize * contractSize) / leverage

        val account = dao.getAccountDirect(accountType)
            ?: return Result.failure(Exception("Account not found"))

        if (account.balance < margin) {
            return Result.failure(Exception("Insufficient balance. Required margin: $%,.2f".format(margin)))
        }

        val position = TradePositionEntity(
            accountType = accountType,
            symbol = symbol,
            direction = direction,
            lotSize = lotSize,
            openPrice = currentPrice,
            currentPrice = currentPrice,
            takeProfit = takeProfit,
            stopLoss = stopLoss,
            leverage = leverage,
            margin = margin,
            openTime = System.currentTimeMillis(),
            profit = 0.0,
            status = "OPEN"
        )

        val id = dao.insertPosition(position)
        return Result.success(id)
    }

    suspend fun placePendingOrder(
        accountType: String,
        symbol: String,
        direction: String,
        pendingType: String,
        triggerPrice: Double,
        lotSize: Double,
        leverage: Int,
        takeProfit: Double?,
        stopLoss: Double?
    ): Result<Long> {
        val instrument = _instruments.value.find { it.symbol == symbol }
            ?: return Result.failure(Exception("Instrument not found"))

        val margin = (triggerPrice * lotSize * instrument.contractSize) / leverage

        val position = TradePositionEntity(
            accountType = accountType,
            symbol = symbol,
            direction = direction,
            lotSize = lotSize,
            openPrice = triggerPrice,
            currentPrice = instrument.price,
            takeProfit = takeProfit,
            stopLoss = stopLoss,
            leverage = leverage,
            margin = margin,
            openTime = System.currentTimeMillis(),
            profit = 0.0,
            status = "PENDING",
            pendingType = pendingType,
            targetTriggerPrice = triggerPrice
        )

        val id = dao.insertPosition(position)
        return Result.success(id)
    }

    suspend fun cancelPendingOrder(orderId: Long) {
        val order = dao.getPositionById(orderId)
        if (order != null && order.status == "PENDING") {
            dao.deletePosition(order)
        }
    }

    suspend fun closePosition(positionId: Long): Result<Double> {
        val position = dao.getPositionById(positionId)
            ?: return Result.failure(Exception("Position not found"))

        if (position.status != "OPEN") {
            return Result.failure(Exception("Position already closed"))
        }

        val instrument = _instruments.value.find { it.symbol == position.symbol }
        val closePrice = instrument?.price ?: position.currentPrice

        val priceDiff = if (position.direction == "BUY") {
            closePrice - position.openPrice
        } else {
            position.openPrice - closePrice
        }
        val contractSize = instrument?.contractSize ?: 1.0
        val realizedProfit = priceDiff * position.lotSize * contractSize

        val closedPosition = position.copy(
            status = "CLOSED",
            closePrice = closePrice,
            closeTime = System.currentTimeMillis(),
            profit = realizedProfit,
            closeReason = "MANUAL"
        )
        dao.updatePosition(closedPosition)

        // Update account balance
        val account = dao.getAccountDirect(position.accountType)
        if (account != null) {
            val newBalance = max(0.0, account.balance + realizedProfit)
            dao.insertOrUpdateAccount(account.copy(balance = newBalance))

            dao.insertTransaction(
                TransactionEntity(
                    accountType = position.accountType,
                    type = if (realizedProfit >= 0) "TRADE_PROFIT" else "TRADE_LOSS",
                    amount = realizedProfit,
                    description = "${position.direction} ${position.symbol} ${position.lotSize} Lots closed"
                )
            )
        }

        return Result.success(realizedProfit)
    }

    suspend fun closeAllPositions(accountType: String) {
        val openPositions = dao.getAllOpenPositionsDirect().filter { it.accountType == accountType }
        for (pos in openPositions) {
            closePosition(pos.id)
        }
    }

    suspend fun updateTpSl(positionId: Long, tp: Double?, sl: Double?) {
        val position = dao.getPositionById(positionId) ?: return
        dao.updatePosition(position.copy(takeProfit = tp, stopLoss = sl))
    }

    suspend fun resetDemoBalance() {
        val account = dao.getAccountDirect("DEMO") ?: AccountEntity(accountType = "DEMO", balance = 10000.0)
        dao.insertOrUpdateAccount(account.copy(balance = 10000.0))
        dao.insertTransaction(
            TransactionEntity(
                accountType = "DEMO",
                type = "DEPOSIT",
                amount = 10000.0,
                description = "Demo Balance Reset to $10,000.00"
            )
        )
    }

    suspend fun depositFunds(accountType: String, amount: Double, method: String) {
        val account = dao.getAccountDirect(accountType) ?: AccountEntity(accountType = accountType, balance = 0.0)
        val newBalance = account.balance + amount
        dao.insertOrUpdateAccount(account.copy(balance = newBalance))
        dao.insertTransaction(
            TransactionEntity(
                accountType = accountType,
                type = "DEPOSIT",
                amount = amount,
                description = "Deposit via $method credited instantly"
            )
        )
    }

    suspend fun withdrawFunds(accountType: String, amount: Double, method: String): Result<Boolean> {
        val account = dao.getAccountDirect(accountType) ?: return Result.failure(Exception("Account not found"))
        if (account.balance < amount) {
            return Result.failure(Exception("Insufficient funds for withdrawal"))
        }
        val newBalance = account.balance - amount
        dao.insertOrUpdateAccount(account.copy(balance = newBalance))
        dao.insertTransaction(
            TransactionEntity(
                accountType = accountType,
                type = "WITHDRAWAL",
                amount = -amount,
                description = "Withdrawal to $method processed"
            )
        )
        return Result.success(true)
    }

    suspend fun claimDailyCheckIn(): Result<Int> {
        val current = dao.getRewardStateDirect() ?: DailyRewardEntity()
        val now = System.currentTimeMillis()
        val newStreak = current.streakDays + 1
        val bonusCoins = newStreak * 100
        val updated = current.copy(
            streakDays = newStreak,
            lastCheckInTimestamp = now,
            couponsCount = current.couponsCount + 1
        )
        dao.insertOrUpdateReward(updated)

        // add coins to accounts
        val demoAcc = dao.getAccountDirect("DEMO")
        if (demoAcc != null) {
            dao.insertOrUpdateAccount(demoAcc.copy(bdcCoins = demoAcc.bdcCoins + bonusCoins))
        }

        return Result.success(bonusCoins)
    }

    suspend fun toggleMining(): Result<Boolean> {
        val current = dao.getRewardStateDirect() ?: DailyRewardEntity()
        val now = System.currentTimeMillis()
        val newState = !current.isMiningActive
        val updated = current.copy(
            isMiningActive = newState,
            miningEndTime = if (newState) now + 86400000 else 0
        )
        dao.insertOrUpdateReward(updated)
        return Result.success(newState)
    }

    private fun startLivePriceSimulation() {
        appScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(1200L)
                updateMarketTicks()
                checkOpenPositionsAndPendingOrders()
            }
        }
    }

    private fun updateMarketTicks() {
        _instruments.update { currentList ->
            currentList.map { item ->
                // random fluctuation (-0.2% to +0.2%)
                val variance = (Random.nextDouble() - 0.49) * 0.004
                val newPrice = item.price * (1.0 + variance)
                val roundedPrice = when (item.decimals) {
                    0 -> kotlin.math.round(newPrice)
                    1 -> kotlin.math.round(newPrice * 10) / 10.0
                    2 -> kotlin.math.round(newPrice * 100) / 100.0
                    4 -> kotlin.math.round(newPrice * 10000) / 10000.0
                    else -> newPrice
                }

                // Update candle
                val updatedCandles = if (item.candles.isNotEmpty()) {
                    val last = item.candles.last()
                    val newHigh = max(last.high, roundedPrice)
                    val newLow = min(last.low, roundedPrice)
                    val updatedLast = last.copy(high = newHigh, low = newLow, close = roundedPrice)
                    item.candles.dropLast(1) + updatedLast
                } else {
                    item.candles
                }

                item.copy(
                    prevPrice = item.price,
                    price = roundedPrice,
                    high24h = max(item.high24h, roundedPrice),
                    low24h = min(item.low24h, roundedPrice),
                    candles = updatedCandles
                )
            }
        }
    }

    private suspend fun checkOpenPositionsAndPendingOrders() {
        val openPositions = dao.getAllOpenPositionsDirect()
        val currentMap = _instruments.value.associateBy { it.symbol }

        for (pos in openPositions) {
            val instrument = currentMap[pos.symbol] ?: continue
            val currentPrice = instrument.price
            val priceDiff = if (pos.direction == "BUY") {
                currentPrice - pos.openPrice
            } else {
                pos.openPrice - currentPrice
            }
            val floatingPnL = priceDiff * pos.lotSize * instrument.contractSize

            // Check Take Profit trigger
            var shouldClose = false
            var closeReason = "MANUAL"
            if (pos.takeProfit != null) {
                if (pos.direction == "BUY" && currentPrice >= pos.takeProfit) {
                    shouldClose = true
                    closeReason = "TAKE_PROFIT"
                } else if (pos.direction == "SELL" && currentPrice <= pos.takeProfit) {
                    shouldClose = true
                    closeReason = "TAKE_PROFIT"
                }
            }

            // Check Stop Loss trigger
            if (!shouldClose && pos.stopLoss != null) {
                if (pos.direction == "BUY" && currentPrice <= pos.stopLoss) {
                    shouldClose = true
                    closeReason = "STOP_LOSS"
                } else if (pos.direction == "SELL" && currentPrice >= pos.stopLoss) {
                    shouldClose = true
                    closeReason = "STOP_LOSS"
                }
            }

            if (shouldClose) {
                val closedPos = pos.copy(
                    status = "CLOSED",
                    closePrice = currentPrice,
                    closeTime = System.currentTimeMillis(),
                    profit = floatingPnL,
                    closeReason = closeReason
                )
                dao.updatePosition(closedPos)
                val account = dao.getAccountDirect(pos.accountType)
                if (account != null) {
                    dao.insertOrUpdateAccount(account.copy(balance = max(0.0, account.balance + floatingPnL)))
                }
            } else {
                // Update live floating price & PnL
                if (pos.currentPrice != currentPrice || pos.profit != floatingPnL) {
                    dao.updatePosition(pos.copy(currentPrice = currentPrice, profit = floatingPnL))
                }
            }
        }
    }

    companion object {
        fun createInitialInstruments(): List<MarketInstrument> {
            val now = System.currentTimeMillis()
            return listOf(
                MarketInstrument(
                    symbol = "BTC/USD",
                    name = "Bitcoin",
                    category = InstrumentCategory.CRYPTO,
                    price = 89250.00,
                    prevPrice = 89100.00,
                    change24h = +3.42,
                    high24h = 89980.00,
                    low24h = 86240.00,
                    volume24h = "3.24B",
                    spread = 2.5,
                    decimals = 2,
                    contractSize = 1.0,
                    defaultLeverage = 100,
                    candles = generateMockCandles(89250.0, 0.008, 40, now)
                ),
                MarketInstrument(
                    symbol = "ETH/USD",
                    name = "Ethereum",
                    category = InstrumentCategory.CRYPTO,
                    price = 3420.50,
                    prevPrice = 3415.00,
                    change24h = +2.15,
                    high24h = 3490.00,
                    low24h = 3310.00,
                    volume24h = "1.85B",
                    spread = 0.4,
                    decimals = 2,
                    contractSize = 1.0,
                    defaultLeverage = 100,
                    candles = generateMockCandles(3420.50, 0.01, 40, now)
                ),
                MarketInstrument(
                    symbol = "SOL/USD",
                    name = "Solana",
                    category = InstrumentCategory.CRYPTO,
                    price = 188.40,
                    prevPrice = 187.90,
                    change24h = +5.60,
                    high24h = 192.50,
                    low24h = 175.20,
                    volume24h = "820M",
                    spread = 0.15,
                    decimals = 2,
                    contractSize = 1.0,
                    defaultLeverage = 50,
                    candles = generateMockCandles(188.40, 0.015, 40, now)
                ),
                MarketInstrument(
                    symbol = "BNB/USD",
                    name = "BNB",
                    category = InstrumentCategory.CRYPTO,
                    price = 598.20,
                    prevPrice = 594.10,
                    change24h = +1.85,
                    high24h = 608.00,
                    low24h = 582.00,
                    volume24h = "640M",
                    spread = 0.25,
                    decimals = 2,
                    contractSize = 1.0,
                    defaultLeverage = 50,
                    candles = generateMockCandles(598.20, 0.01, 40, now)
                ),
                MarketInstrument(
                    symbol = "DOGE/USD",
                    name = "Dogecoin",
                    category = InstrumentCategory.CRYPTO,
                    price = 0.2240,
                    prevPrice = 0.2190,
                    change24h = +4.30,
                    high24h = 0.2380,
                    low24h = 0.2080,
                    volume24h = "450M",
                    spread = 0.0002,
                    decimals = 4,
                    contractSize = 1000.0,
                    defaultLeverage = 50,
                    candles = generateMockCandles(0.2240, 0.02, 40, now)
                ),
                MarketInstrument(
                    symbol = "XAU/USD",
                    name = "Gold / US Dollar",
                    category = InstrumentCategory.COMMODITIES,
                    price = 2728.80,
                    prevPrice = 2726.50,
                    change24h = +0.85,
                    high24h = 2742.00,
                    low24h = 2708.50,
                    volume24h = "980M",
                    spread = 0.35,
                    decimals = 2,
                    contractSize = 100.0,
                    defaultLeverage = 100,
                    candles = generateMockCandles(2728.80, 0.004, 40, now)
                ),
                MarketInstrument(
                    symbol = "EUR/USD",
                    name = "Euro / US Dollar",
                    category = InstrumentCategory.FOREX,
                    price = 1.0845,
                    prevPrice = 1.0850,
                    change24h = -0.18,
                    high24h = 1.0892,
                    low24h = 1.0830,
                    volume24h = "4.6B",
                    spread = 0.0001,
                    decimals = 4,
                    contractSize = 100000.0,
                    defaultLeverage = 200,
                    candles = generateMockCandles(1.0845, 0.0015, 40, now)
                ),
                MarketInstrument(
                    symbol = "GBP/USD",
                    name = "British Pound / USD",
                    category = InstrumentCategory.FOREX,
                    price = 1.2980,
                    prevPrice = 1.2965,
                    change24h = +0.32,
                    high24h = 1.3040,
                    low24h = 1.2940,
                    volume24h = "2.8B",
                    spread = 0.0002,
                    decimals = 4,
                    contractSize = 100000.0,
                    defaultLeverage = 200,
                    candles = generateMockCandles(1.2980, 0.0018, 40, now)
                ),
                MarketInstrument(
                    symbol = "USD/JPY",
                    name = "US Dollar / Japanese Yen",
                    category = InstrumentCategory.FOREX,
                    price = 153.60,
                    prevPrice = 153.85,
                    change24h = -0.45,
                    high24h = 154.20,
                    low24h = 152.90,
                    volume24h = "3.1B",
                    spread = 0.02,
                    decimals = 2,
                    contractSize = 100000.0,
                    defaultLeverage = 200,
                    candles = generateMockCandles(153.60, 0.002, 40, now)
                ),
                MarketInstrument(
                    symbol = "USOIL",
                    name = "WTI Crude Oil",
                    category = InstrumentCategory.COMMODITIES,
                    price = 72.45,
                    prevPrice = 73.10,
                    change24h = -1.10,
                    high24h = 74.20,
                    low24h = 71.80,
                    volume24h = "640M",
                    spread = 0.04,
                    decimals = 2,
                    contractSize = 1000.0,
                    defaultLeverage = 50,
                    candles = generateMockCandles(72.45, 0.008, 40, now)
                ),
                MarketInstrument(
                    symbol = "NAS100",
                    name = "Nasdaq 100 Index",
                    category = InstrumentCategory.INDICES,
                    price = 20480.00,
                    prevPrice = 20390.00,
                    change24h = +1.28,
                    high24h = 20560.00,
                    low24h = 20310.00,
                    volume24h = "1.9B",
                    spread = 1.2,
                    decimals = 2,
                    contractSize = 1.0,
                    defaultLeverage = 100,
                    candles = generateMockCandles(20480.00, 0.006, 40, now)
                ),
                MarketInstrument(
                    symbol = "US30",
                    name = "Wall Street 30 / Dow",
                    category = InstrumentCategory.INDICES,
                    price = 43890.00,
                    prevPrice = 43820.00,
                    change24h = +0.54,
                    high24h = 44020.00,
                    low24h = 43680.00,
                    volume24h = "1.4B",
                    spread = 2.0,
                    decimals = 2,
                    contractSize = 1.0,
                    defaultLeverage = 100,
                    candles = generateMockCandles(43890.00, 0.005, 40, now)
                )
            )
        }

        private fun generateMockCandles(
            basePrice: Double,
            volatility: Double,
            count: Int,
            endTime: Long
        ): List<Candle> {
            val list = mutableListOf<Candle>()
            var currentClose = basePrice * (1.0 - (count * 0.001))
            val interval = 60000L * 15 // 15 min candles

            for (i in 0 until count) {
                val open = currentClose
                val change = (Random.nextDouble() - 0.48) * volatility * basePrice
                val close = open + change
                val high = max(open, close) + Random.nextDouble() * volatility * basePrice * 0.6
                val low = min(open, close) - Random.nextDouble() * volatility * basePrice * 0.6
                val time = endTime - ((count - i) * interval)
                list.add(
                    Candle(
                        timestamp = time,
                        open = open,
                        high = high,
                        low = low,
                        close = close,
                        volume = Random.nextDouble(50.0, 500.0)
                    )
                )
                currentClose = close
            }
            return list
        }

        fun createInitialSignals(): List<TradingSignal> {
            return listOf(
                TradingSignal(
                    id = "SIG-1",
                    symbol = "BTC/USD",
                    direction = SignalDirection.BUY,
                    entryPrice = 88900.0,
                    targetPrice1 = 91500.0,
                    targetPrice2 = 93800.0,
                    stopLoss = 87200.0,
                    confidence = 94,
                    timeAgo = "12m ago",
                    rationale = "Bullish pennant breakout with rising on-chain volume and strong support above 88k EMA25."
                ),
                TradingSignal(
                    id = "SIG-2",
                    symbol = "XAU/USD",
                    direction = SignalDirection.BUY,
                    entryPrice = 2724.0,
                    targetPrice1 = 2748.0,
                    targetPrice2 = 2765.0,
                    stopLoss = 2708.0,
                    confidence = 89,
                    timeAgo = "35m ago",
                    rationale = "Safe haven demand surges as central banks continue accumulation. Golden cross formed on 4H chart."
                ),
                TradingSignal(
                    id = "SIG-3",
                    symbol = "EUR/USD",
                    direction = SignalDirection.SELL,
                    entryPrice = 1.0860,
                    targetPrice1 = 1.0810,
                    targetPrice2 = 1.0760,
                    stopLoss = 1.0910,
                    confidence = 85,
                    timeAgo = "1h ago",
                    rationale = "ECB dovish rate guidance vs resilient US economic data creates downside pressure below 1.0880."
                ),
                TradingSignal(
                    id = "SIG-4",
                    symbol = "NAS100",
                    direction = SignalDirection.BUY,
                    entryPrice = 20420.0,
                    targetPrice1 = 20750.0,
                    targetPrice2 = 21000.0,
                    stopLoss = 20200.0,
                    confidence = 91,
                    timeAgo = "2h ago",
                    rationale = "Tech earnings momentum remains robust with semiconductor and AI leaders posting higher guidance."
                )
            )
        }

        fun createInitialCalendarEvents(): List<EconomicEvent> {
            return listOf(
                EconomicEvent(
                    id = "CAL-1",
                    time = "14:30 GMT",
                    currency = "USD",
                    countryFlag = "🇺🇸",
                    title = "Non-Farm Employment Change (NFP)",
                    impact = ImpactLevel.HIGH,
                    actual = "225K",
                    forecast = "180K",
                    previous = "142K"
                ),
                EconomicEvent(
                    id = "CAL-2",
                    time = "18:00 GMT",
                    currency = "USD",
                    countryFlag = "🇺🇸",
                    title = "FOMC Fed Interest Rate Decision",
                    impact = ImpactLevel.HIGH,
                    actual = "4.75%",
                    forecast = "4.75%",
                    previous = "5.00%"
                ),
                EconomicEvent(
                    id = "CAL-3",
                    time = "12:45 GMT",
                    currency = "EUR",
                    countryFlag = "🇪🇺",
                    title = "ECB Deposit Facility Rate",
                    impact = ImpactLevel.HIGH,
                    actual = null,
                    forecast = "3.25%",
                    previous = "3.50%"
                ),
                EconomicEvent(
                    id = "CAL-4",
                    time = "09:30 GMT",
                    currency = "GBP",
                    countryFlag = "🇬🇧",
                    title = "UK Consumer Price Index (CPI) YoY",
                    impact = ImpactLevel.MEDIUM,
                    actual = "2.1%",
                    forecast = "2.2%",
                    previous = "2.4%"
                )
            )
        }

        fun createInitialNews(): List<MarketNews> {
            return listOf(
                MarketNews(
                    id = "NEWS-1",
                    title = "Bitcoin Surges Past $89,000 as Institutional Spot Inflows Break Records",
                    source = "Bloomberg Crypto",
                    timeAgo = "18m ago",
                    category = "Crypto",
                    summary = "Bitcoin recorded its strongest weekly rally this quarter, driven by continuous institutional allocations and ETF volumes exceeding $4.2B daily.",
                    readMinutes = 3
                ),
                MarketNews(
                    id = "NEWS-2",
                    title = "Gold Hits Fresh All-Time High Amid Geopolitical Uncertainties and Rate Cuts",
                    source = "Reuters Finance",
                    timeAgo = "42m ago",
                    category = "Metals",
                    summary = "Spot gold reached $2,740 per ounce as market participants price in another 25 basis point Federal Reserve interest rate reduction.",
                    readMinutes = 2
                ),
                MarketNews(
                    id = "NEWS-3",
                    title = "US Dollar Index Consolidates Above 104 as Bond Yields Stabilize",
                    source = "FXStreet",
                    timeAgo = "1h ago",
                    category = "Forex",
                    summary = "The greenback traded firmly against major peers with EUR/USD and GBP/USD hovering near key technical support levels.",
                    readMinutes = 4
                )
            )
        }
    }
}
