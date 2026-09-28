package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddAssetDialog
import com.example.ui.components.DanaTopBar
import com.example.ui.components.DepositBottomSheet
import com.example.ui.components.EditTpSlBottomSheet
import com.example.ui.components.WithdrawBottomSheet
import com.example.ui.screens.MarketsScreen
import com.example.ui.screens.PortfolioScreen
import com.example.ui.screens.PositionsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SignalsScreen
import com.example.ui.screens.TradeScreen
import com.example.ui.theme.DanaBgDark
import com.example.ui.theme.DanaGold
import com.example.ui.theme.DanaSurfaceDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.DanaTab
import com.example.ui.viewmodel.TradingViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BtcDanaApp()
            }
        }
    }
}

@Composable
fun BtcDanaApp(viewModel: TradingViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val instruments by viewModel.instruments.collectAsStateWithLifecycle()
    val signals by viewModel.signals.collectAsStateWithLifecycle()
    val calendarEvents by viewModel.calendarEvents.collectAsStateWithLifecycle()
    val news by viewModel.news.collectAsStateWithLifecycle()
    val account by viewModel.currentAccount.collectAsStateWithLifecycle()
    val openPositions by viewModel.openPositions.collectAsStateWithLifecycle()
    val pendingPositions by viewModel.pendingPositions.collectAsStateWithLifecycle()
    val closedPositions by viewModel.closedPositions.collectAsStateWithLifecycle()
    val rewardState by viewModel.rewardState.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Handle back button on sub-screens
    if (uiState.selectedTab != DanaTab.MARKETS) {
        BackHandler {
            viewModel.selectTab(DanaTab.MARKETS)
        }
    }

    val selectedInstrument = instruments.find { it.symbol == uiState.selectedSymbol }
        ?: instruments.firstOrNull()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DanaBgDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DanaTopBar(
                currentAccountType = uiState.accountType,
                account = account,
                onSwitchAccount = { viewModel.switchAccountType(it) },
                onDepositClick = { viewModel.openDepositSheet() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DanaSurfaceDark,
                tonalElevation = 4.dp
            ) {
                // 1. Markets
                NavigationBarItem(
                    selected = uiState.selectedTab == DanaTab.MARKETS,
                    onClick = { viewModel.selectTab(DanaTab.MARKETS) },
                    icon = { Icon(Icons.Default.Store, contentDescription = "Markets", modifier = Modifier.size(22.dp)) },
                    label = { Text("Markets", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = DanaGold,
                        indicatorColor = DanaGold,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_markets")
                )

                // 2. Trade
                NavigationBarItem(
                    selected = uiState.selectedTab == DanaTab.TRADE,
                    onClick = { viewModel.selectTab(DanaTab.TRADE) },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "Trade", modifier = Modifier.size(22.dp)) },
                    label = { Text("Trade", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = DanaGold,
                        indicatorColor = DanaGold,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_trade")
                )

                // 3. Portfolio (Crypto Holdings Tracking)
                NavigationBarItem(
                    selected = uiState.selectedTab == DanaTab.PORTFOLIO,
                    onClick = { viewModel.selectTab(DanaTab.PORTFOLIO) },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Portfolio", modifier = Modifier.size(22.dp)) },
                    label = { Text("Portfolio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = DanaGold,
                        indicatorColor = DanaGold,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_portfolio")
                )

                // 4. Positions
                NavigationBarItem(
                    selected = uiState.selectedTab == DanaTab.POSITIONS,
                    onClick = { viewModel.selectTab(DanaTab.POSITIONS) },
                    icon = {
                        if (openPositions.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = DanaGold, contentColor = Color.Black) {
                                        Text("${openPositions.size}")
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Assessment, contentDescription = "Positions", modifier = Modifier.size(22.dp))
                            }
                        } else {
                            Icon(Icons.Default.Assessment, contentDescription = "Positions", modifier = Modifier.size(22.dp))
                        }
                    },
                    label = { Text("Positions", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = DanaGold,
                        indicatorColor = DanaGold,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_positions")
                )

                // 5. Profile / Me
                NavigationBarItem(
                    selected = uiState.selectedTab == DanaTab.PROFILE,
                    onClick = { viewModel.selectTab(DanaTab.PROFILE) },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Me", modifier = Modifier.size(22.dp)) },
                    label = { Text("Me", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = DanaGold,
                        indicatorColor = DanaGold,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        }
    ) { innerPadding ->
        when (uiState.selectedTab) {
            DanaTab.MARKETS -> {
                MarketsScreen(
                    instruments = instruments,
                    selectedCategory = uiState.selectedCategory,
                    searchQuery = uiState.searchQuery,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                    onInstrumentClick = { symbol -> viewModel.navigateToTradeWithInstrument(symbol) },
                    onToggleFavorite = { symbol -> viewModel.toggleFavorite(symbol) },
                    onOpenSignals = { viewModel.selectTab(DanaTab.SIGNALS) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            DanaTab.TRADE -> {
                TradeScreen(
                    instrument = selectedInstrument,
                    allInstruments = instruments,
                    account = account,
                    isCandleChart = uiState.isCandleChart,
                    activeIndicator = uiState.activeIndicator,
                    chartTimeframe = uiState.chartTimeframe,
                    orderType = uiState.orderType,
                    orderDirection = uiState.orderDirection,
                    lotSize = uiState.lotSize,
                    leverage = uiState.leverage,
                    isTpEnabled = uiState.isTpEnabled,
                    tpPrice = uiState.tpPrice,
                    isSlEnabled = uiState.isSlEnabled,
                    slPrice = uiState.slPrice,
                    pendingTriggerPrice = uiState.pendingTriggerPrice,
                    pendingType = uiState.pendingType,
                    onSelectInstrument = { viewModel.selectInstrument(it) },
                    onTimeframeChange = { viewModel.setChartTimeframe(it) },
                    onToggleChartType = { viewModel.toggleChartType() },
                    onIndicatorChange = { viewModel.setActiveIndicator(it) },
                    onOrderTypeChange = { viewModel.setOrderType(it) },
                    onOrderDirectionChange = { viewModel.setOrderDirection(it) },
                    onLotSizeChange = { viewModel.setLotSize(it) },
                    onLeverageChange = { viewModel.setLeverage(it) },
                    onTpEnabledChange = { enabled, defaultPrice -> viewModel.setTpEnabled(enabled, defaultPrice) },
                    onTpPriceChange = { viewModel.setTpPrice(it) },
                    onSlEnabledChange = { enabled, defaultPrice -> viewModel.setSlEnabled(enabled, defaultPrice) },
                    onSlPriceChange = { viewModel.setSlPrice(it) },
                    onPendingTriggerChange = { viewModel.setPendingTriggerPrice(it) },
                    onPendingTypeChange = { viewModel.setPendingType(it) },
                    onExecuteOrder = { viewModel.executeOrder() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            DanaTab.PORTFOLIO -> {
                PortfolioScreen(
                    account = account,
                    holdings = holdings,
                    isHideBalance = uiState.isHideBalance,
                    onToggleHideBalance = { viewModel.toggleHideBalance() },
                    onOpenAddAsset = { viewModel.openAddAsset() },
                    onOpenDeposit = { viewModel.openDepositSheet() },
                    onNavigateToTrade = { symbol -> viewModel.navigateToTradeWithInstrument(symbol) },
                    onDeleteHolding = { id -> viewModel.deleteHolding(id) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            DanaTab.POSITIONS -> {
                PositionsScreen(
                    account = account,
                    openPositions = openPositions,
                    pendingPositions = pendingPositions,
                    closedPositions = closedPositions,
                    onClosePosition = { viewModel.closePosition(it) },
                    onCloseAllPositions = { viewModel.closeAllPositions() },
                    onCancelPendingOrder = { viewModel.cancelPendingOrder(it) },
                    onEditTpSl = { viewModel.openEditTpSl(it) },
                    onNavigateToTrade = { viewModel.selectTab(DanaTab.TRADE) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            DanaTab.SIGNALS -> {
                SignalsScreen(
                    signals = signals,
                    calendarEvents = calendarEvents,
                    news = news,
                    onApplySignal = { sig -> viewModel.applySignalToTrade(sig) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            DanaTab.PROFILE -> {
                ProfileScreen(
                    currentAccountType = uiState.accountType,
                    account = account,
                    rewardState = rewardState,
                    onSwitchAccount = { viewModel.switchAccountType(it) },
                    onOpenDeposit = { viewModel.openDepositSheet() },
                    onOpenWithdraw = { viewModel.openWithdrawSheet() },
                    onResetDemoBalance = { viewModel.resetDemoBalance() },
                    onClaimCheckIn = { viewModel.claimDailyCheckIn() },
                    onToggleMining = { viewModel.toggleMining() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        // Dialogs & Sheets
        if (uiState.isAddAssetOpen) {
            AddAssetDialog(
                instruments = instruments,
                onDismiss = { viewModel.dismissAddAsset() },
                onAddHolding = { symbol, name, amount, buyPrice, colorHex ->
                    viewModel.addCryptoHolding(symbol, name, amount, buyPrice, colorHex)
                }
            )
        }

        if (uiState.isDepositOpen) {
            DepositBottomSheet(
                accountType = uiState.accountType,
                onDismiss = { viewModel.dismissDepositSheet() },
                onDeposit = { amount, method -> viewModel.depositFunds(amount, method) }
            )
        }

        if (uiState.isWithdrawOpen) {
            WithdrawBottomSheet(
                accountType = uiState.accountType,
                balance = account?.balance ?: 0.0,
                onDismiss = { viewModel.dismissWithdrawSheet() },
                onWithdraw = { amount, method -> viewModel.withdrawFunds(amount, method) }
            )
        }

        if (uiState.isEditTpSlOpen && uiState.editingPosition != null) {
            EditTpSlBottomSheet(
                position = uiState.editingPosition!!,
                onDismiss = { viewModel.dismissEditTpSl() },
                onSave = { posId, tp, sl -> viewModel.saveEditedTpSl(posId, tp, sl) }
            )
        }
    }
}
