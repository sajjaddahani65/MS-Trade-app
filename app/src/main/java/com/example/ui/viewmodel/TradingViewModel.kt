package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AccountEntity
import com.example.data.local.DailyRewardEntity
import com.example.data.local.DanaDatabase
import com.example.data.local.TradePositionEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.EconomicEvent
import com.example.data.model.HoldingItem
import com.example.data.model.InstrumentCategory
import com.example.data.model.MarketInstrument
import com.example.data.model.MarketNews
import com.example.data.model.SignalDirection
import com.example.data.model.TradingSignal
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

enum class DanaTab {
    MARKETS,
    TRADE,
    PORTFOLIO,
    POSITIONS,
    SIGNALS,
    PROFILE
}

data class TradingUiState(
    val accountType: String = "DEMO", // "DEMO" or "REAL"
    val selectedTab: DanaTab = DanaTab.MARKETS,
    val selectedCategory: InstrumentCategory = InstrumentCategory.ALL,
    val searchQuery: String = "",
    val selectedSymbol: String = "BTC/USD",
    val chartTimeframe: String = "15M",
    val isCandleChart: Boolean = true,
    val activeIndicator: String = "MA", // "MA", "BOLL", "NONE"
    // Order form state
    val orderType: String = "MARKET", // "MARKET" or "PENDING"
    val orderDirection: String = "BUY", // "BUY" or "SELL"
    val lotSize: Double = 0.10,
    val leverage: Int = 100,
    val isTpEnabled: Boolean = false,
    val tpPrice: Double? = null,
    val isSlEnabled: Boolean = false,
    val slPrice: Double? = null,
    val pendingTriggerPrice: Double? = null,
    val pendingType: String = "BUY_LIMIT",
    // Dialog & sheet states
    val isDepositOpen: Boolean = false,
    val isWithdrawOpen: Boolean = false,
    val isEditTpSlOpen: Boolean = false,
    val editingPosition: TradePositionEntity? = null,
    val isAddAssetOpen: Boolean = false,
    val isHideBalance: Boolean = false,
    val userNotification: String? = null
)

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DanaDatabase.getDatabase(application, viewModelScope)
    private val repository = TradingRepository(database.danaDao(), viewModelScope)

    private val _uiState = MutableStateFlow(TradingUiState())
    val uiState: StateFlow<TradingUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    val instruments: StateFlow<List<MarketInstrument>> = repository.instruments
    val signals: StateFlow<List<TradingSignal>> = repository.signals
    val calendarEvents: StateFlow<List<EconomicEvent>> = repository.calendarEvents
    val news: StateFlow<List<MarketNews>> = repository.news

    private val watchlist = repository.watchlist.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current Account
    val currentAccount: StateFlow<AccountEntity?> = _uiState
        .map { it.accountType }
        .combine(repository.getAccount("DEMO")) { accType, _ -> accType }
        .combine(repository.getAccount("REAL")) { accType, realAcc ->
            // Triggered on changes
            accType
        }
        .combine(repository.getAccount("DEMO")) { _, demoAcc ->
            if (_uiState.value.accountType == "DEMO") demoAcc else null
        }
        .combine(repository.getAccount("REAL")) { demoAcc, realAcc ->
            if (_uiState.value.accountType == "DEMO") demoAcc else realAcc
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AccountEntity(accountType = "DEMO", balance = 10000.0)
        )

    // Open positions for current account
    val openPositions: StateFlow<List<TradePositionEntity>> = combine(
        _uiState.map { it.accountType },
        repository.getPositions("DEMO", "OPEN"),
        repository.getPositions("REAL", "OPEN")
    ) { accType, demoList, realList ->
        if (accType == "DEMO") demoList else realList
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Pending orders for current account
    val pendingPositions: StateFlow<List<TradePositionEntity>> = combine(
        _uiState.map { it.accountType },
        repository.getPositions("DEMO", "PENDING"),
        repository.getPositions("REAL", "PENDING")
    ) { accType, demoList, realList ->
        if (accType == "DEMO") demoList else realList
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Closed trade history for current account
    val closedPositions: StateFlow<List<TradePositionEntity>> = combine(
        _uiState.map { it.accountType },
        repository.getPositions("DEMO", "CLOSED"),
        repository.getPositions("REAL", "CLOSED")
    ) { accType, demoList, realList ->
        if (accType == "DEMO") demoList else realList
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Transactions
    val transactions: StateFlow<List<TransactionEntity>> = combine(
        _uiState.map { it.accountType },
        repository.getTransactions("DEMO"),
        repository.getTransactions("REAL")
    ) { accType, demoTx, realTx ->
        if (accType == "DEMO") demoTx else realTx
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Daily reward state
    val rewardState: StateFlow<DailyRewardEntity?> = repository.getRewardState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyRewardEntity()
    )

    // Crypto Portfolio Holdings combined with live instrument market prices
    private val rawHoldings = combine(
        _uiState.map { it.accountType },
        repository.getHoldings("DEMO"),
        repository.getHoldings("REAL")
    ) { accType, demoList, realList ->
        if (accType == "DEMO") demoList else realList
    }

    val holdings: StateFlow<List<HoldingItem>> = combine(
        rawHoldings,
        instruments
    ) { rawList, instList ->
        val instMap = instList.associateBy { it.symbol.substringBefore("/") }

        val preliminary = rawList.map { entity ->
            val inst = instMap[entity.symbol]
            val livePrice = inst?.price ?: entity.avgBuyPrice
            val currentValue = entity.amount * livePrice
            val costBasis = entity.amount * entity.avgBuyPrice
            val pnl = currentValue - costBasis
            val pnlPct = if (costBasis > 0) (pnl / costBasis) * 100.0 else 0.0
            val chg24 = inst?.change24h ?: 0.0
            val valChg24 = currentValue * (chg24 / 100.0)

            HoldingItem(
                id = entity.id,
                symbol = entity.symbol,
                name = entity.name,
                amount = entity.amount,
                avgBuyPrice = entity.avgBuyPrice,
                currentPrice = livePrice,
                currentValue = currentValue,
                costBasis = costBasis,
                totalPnL = pnl,
                totalPnLPercent = pnlPct,
                change24h = chg24,
                valueChange24h = valChg24,
                allocationPercent = 0.0,
                colorHex = entity.colorHex
            )
        }

        val totalValue = preliminary.sumOf { it.currentValue }
        preliminary.map { item ->
            val alloc = if (totalValue > 0) (item.currentValue / totalValue) * 100.0 else 0.0
            item.copy(allocationPercent = alloc)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun openAddAsset() {
        _uiState.update { it.copy(isAddAssetOpen = true) }
    }

    fun dismissAddAsset() {
        _uiState.update { it.copy(isAddAssetOpen = false) }
    }

    fun toggleHideBalance() {
        _uiState.update { it.copy(isHideBalance = !it.isHideBalance) }
    }

    fun addCryptoHolding(symbol: String, name: String, amount: Double, buyPrice: Double, colorHex: String) {
        viewModelScope.launch {
            repository.addOrUpdateHolding(
                accountType = _uiState.value.accountType,
                symbol = symbol,
                name = name,
                amount = amount,
                buyPrice = buyPrice,
                colorHex = colorHex
            )
            dismissAddAsset()
            _eventFlow.emit("Added $amount $symbol to Portfolio")
        }
    }

    fun deleteHolding(id: Long) {
        viewModelScope.launch {
            repository.deleteHolding(id)
            _eventFlow.emit("Holding removed")
        }
    }

    fun selectTab(tab: DanaTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun switchAccountType(type: String) {
        _uiState.update { it.copy(accountType = type) }
        viewModelScope.launch {
            _eventFlow.emit("Switched to $type Account")
        }
    }

    fun selectCategory(category: InstrumentCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectInstrument(symbol: String) {
        _uiState.update { it.copy(selectedSymbol = symbol) }
    }

    fun navigateToTradeWithInstrument(symbol: String, direction: String = "BUY") {
        _uiState.update {
            it.copy(
                selectedSymbol = symbol,
                orderDirection = direction,
                selectedTab = DanaTab.TRADE
            )
        }
    }

    fun applySignalToTrade(signal: TradingSignal) {
        val dir = if (signal.direction == SignalDirection.BUY) "BUY" else "SELL"
        _uiState.update {
            it.copy(
                selectedSymbol = signal.symbol,
                orderDirection = dir,
                isTpEnabled = true,
                tpPrice = signal.targetPrice1,
                isSlEnabled = true,
                slPrice = signal.stopLoss,
                selectedTab = DanaTab.TRADE
            )
        }
        viewModelScope.launch {
            _eventFlow.emit("Signal applied: ${signal.symbol} $dir")
        }
    }

    fun setChartTimeframe(tf: String) {
        _uiState.update { it.copy(chartTimeframe = tf) }
    }

    fun toggleChartType() {
        _uiState.update { it.copy(isCandleChart = !it.isCandleChart) }
    }

    fun setActiveIndicator(ind: String) {
        _uiState.update { it.copy(activeIndicator = ind) }
    }

    fun setOrderType(type: String) {
        _uiState.update { it.copy(orderType = type) }
    }

    fun setOrderDirection(dir: String) {
        _uiState.update { it.copy(orderDirection = dir) }
    }

    fun setLotSize(size: Double) {
        val cleanSize = kotlin.math.round(max(0.01, size) * 100) / 100.0
        _uiState.update { it.copy(lotSize = cleanSize) }
    }

    fun setLeverage(lev: Int) {
        _uiState.update { it.copy(leverage = lev) }
    }

    fun setTpEnabled(enabled: Boolean, defaultPrice: Double? = null) {
        _uiState.update { it.copy(isTpEnabled = enabled, tpPrice = defaultPrice ?: it.tpPrice) }
    }

    fun setTpPrice(price: Double?) {
        _uiState.update { it.copy(tpPrice = price) }
    }

    fun setSlEnabled(enabled: Boolean, defaultPrice: Double? = null) {
        _uiState.update { it.copy(isSlEnabled = enabled, slPrice = defaultPrice ?: it.slPrice) }
    }

    fun setSlPrice(price: Double?) {
        _uiState.update { it.copy(slPrice = price) }
    }

    fun setPendingTriggerPrice(price: Double?) {
        _uiState.update { it.copy(pendingTriggerPrice = price) }
    }

    fun setPendingType(type: String) {
        _uiState.update { it.copy(pendingType = type) }
    }

    fun executeOrder() {
        val state = _uiState.value
        val symbol = state.selectedSymbol
        val direction = state.orderDirection
        val lotSize = state.lotSize
        val leverage = state.leverage
        val tp = if (state.isTpEnabled) state.tpPrice else null
        val sl = if (state.isSlEnabled) state.slPrice else null

        viewModelScope.launch {
            if (state.orderType == "MARKET") {
                val result = repository.openPosition(
                    accountType = state.accountType,
                    symbol = symbol,
                    direction = direction,
                    lotSize = lotSize,
                    leverage = leverage,
                    takeProfit = tp,
                    stopLoss = sl
                )
                if (result.isSuccess) {
                    _eventFlow.emit("Order Executed: $direction $symbol $lotSize lots")
                    _uiState.update { it.copy(selectedTab = DanaTab.POSITIONS) }
                } else {
                    _eventFlow.emit(result.exceptionOrNull()?.message ?: "Execution failed")
                }
            } else {
                val trigger = state.pendingTriggerPrice ?: return@launch
                val result = repository.placePendingOrder(
                    accountType = state.accountType,
                    symbol = symbol,
                    direction = direction,
                    pendingType = state.pendingType,
                    triggerPrice = trigger,
                    lotSize = lotSize,
                    leverage = leverage,
                    takeProfit = tp,
                    stopLoss = sl
                )
                if (result.isSuccess) {
                    _eventFlow.emit("Pending Order Placed: $symbol @ $trigger")
                    _uiState.update { it.copy(selectedTab = DanaTab.POSITIONS) }
                } else {
                    _eventFlow.emit(result.exceptionOrNull()?.message ?: "Pending order failed")
                }
            }
        }
    }

    fun closePosition(positionId: Long) {
        viewModelScope.launch {
            val result = repository.closePosition(positionId)
            if (result.isSuccess) {
                val profit = result.getOrNull() ?: 0.0
                val sign = if (profit >= 0) "+$" else "-$"
                _eventFlow.emit("Position Closed: $sign%,.2f".format(kotlin.math.abs(profit)))
            } else {
                _eventFlow.emit(result.exceptionOrNull()?.message ?: "Close failed")
            }
        }
    }

    fun closeAllPositions() {
        viewModelScope.launch {
            repository.closeAllPositions(_uiState.value.accountType)
            _eventFlow.emit("All ${_uiState.value.accountType} positions closed")
        }
    }

    fun cancelPendingOrder(orderId: Long) {
        viewModelScope.launch {
            repository.cancelPendingOrder(orderId)
            _eventFlow.emit("Pending order canceled")
        }
    }

    fun openEditTpSl(position: TradePositionEntity) {
        _uiState.update {
            it.copy(
                isEditTpSlOpen = true,
                editingPosition = position
            )
        }
    }

    fun dismissEditTpSl() {
        _uiState.update { it.copy(isEditTpSlOpen = false, editingPosition = null) }
    }

    fun saveEditedTpSl(positionId: Long, tp: Double?, sl: Double?) {
        viewModelScope.launch {
            repository.updateTpSl(positionId, tp, sl)
            dismissEditTpSl()
            _eventFlow.emit("TP/SL Updated")
        }
    }

    fun toggleFavorite(symbol: String) {
        viewModelScope.launch {
            val isFav = watchlist.value.any { it.symbol == symbol }
            repository.toggleWatchlist(symbol, isFav)
        }
    }

    fun resetDemoBalance() {
        viewModelScope.launch {
            repository.resetDemoBalance()
            _eventFlow.emit("Demo balance reset to $10,000.00")
        }
    }

    fun openDepositSheet() {
        _uiState.update { it.copy(isDepositOpen = true) }
    }

    fun dismissDepositSheet() {
        _uiState.update { it.copy(isDepositOpen = false) }
    }

    fun depositFunds(amount: Double, method: String) {
        viewModelScope.launch {
            repository.depositFunds(_uiState.value.accountType, amount, method)
            dismissDepositSheet()
            _eventFlow.emit("Deposited $%,.2f via $method".format(amount))
        }
    }

    fun openWithdrawSheet() {
        _uiState.update { it.copy(isWithdrawOpen = true) }
    }

    fun dismissWithdrawSheet() {
        _uiState.update { it.copy(isWithdrawOpen = false) }
    }

    fun withdrawFunds(amount: Double, method: String) {
        viewModelScope.launch {
            val result = repository.withdrawFunds(_uiState.value.accountType, amount, method)
            if (result.isSuccess) {
                dismissWithdrawSheet()
                _eventFlow.emit("Withdrawal of $%,.2f submitted".format(amount))
            } else {
                _eventFlow.emit(result.exceptionOrNull()?.message ?: "Withdrawal failed")
            }
        }
    }

    fun claimDailyCheckIn() {
        viewModelScope.launch {
            val result = repository.claimDailyCheckIn()
            if (result.isSuccess) {
                _eventFlow.emit("Check-in Bonus Claimed: +${result.getOrNull()} BDC Coins!")
            }
        }
    }

    fun toggleMining() {
        viewModelScope.launch {
            val result = repository.toggleMining()
            val state = result.getOrNull() ?: false
            _eventFlow.emit(if (state) "BDC Cloud Mining Started (145 MH/s)" else "Mining Paused")
        }
    }
}
