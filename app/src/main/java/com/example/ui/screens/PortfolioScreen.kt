package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AccountEntity
import com.example.data.model.HoldingItem
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
import com.example.ui.theme.DanaBgDark
import com.example.ui.theme.DanaBorderDark
import com.example.ui.theme.DanaGold
import com.example.ui.theme.DanaSurfaceDark
import com.example.ui.theme.DanaSurfaceElevatedDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlin.math.abs

@Composable
fun PortfolioScreen(
    account: AccountEntity?,
    holdings: List<HoldingItem>,
    isHideBalance: Boolean,
    onToggleHideBalance: () -> Unit,
    onOpenAddAsset: () -> Unit,
    onOpenDeposit: () -> Unit,
    onNavigateToTrade: (String) -> Unit,
    onDeleteHolding: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalHoldingsValue = holdings.sumOf { it.currentValue }
    val cashBalance = account?.balance ?: 0.0
    val totalPortfolioNetWorth = totalHoldingsValue + cashBalance
    val totalCostBasis = holdings.sumOf { it.costBasis }
    val totalAllTimePnL = totalHoldingsValue - totalCostBasis
    val totalAllTimePnLPct = if (totalCostBasis > 0) (totalAllTimePnL / totalCostBasis) * 100.0 else 0.0
    val total24hValueChange = holdings.sumOf { it.valueChange24h }
    val total24hChangePct = if (totalHoldingsValue > 0) (total24hValueChange / totalHoldingsValue) * 100.0 else 0.0

    val is24hUp = total24hValueChange >= 0
    val isAllTimeUp = totalAllTimePnL >= 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DanaBgDark),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Portfolio Net Worth Header Card
        item {
            Surface(
                color = DanaSurfaceDark,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("portfolio_summary_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title and Eye toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Total Portfolio Balance",
                                color = TextMutedDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = onToggleHideBalance,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (isHideBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Balance",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DanaGold.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Live Market USD",
                                color = DanaGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Large Total Balance
                    Text(
                        text = if (isHideBalance) "••••••••" else "$%,.2f".format(totalPortfolioNetWorth),
                        color = TextPrimaryDark,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 24h P&L and All-Time P&L Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 24h P&L pill
                        Surface(
                            color = if (is24hUp) BullGreen.copy(alpha = 0.12f) else BearRed.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (is24hUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (is24hUp) BullGreen else BearRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text("24h P&L", color = TextMutedDark, fontSize = 9.sp)
                                    Text(
                                        text = if (isHideBalance) "•••" else "${if (is24hUp) "+" else "-"}$%,.2f (%,.1f%%)".format(abs(total24hValueChange), abs(total24hChangePct)),
                                        color = if (is24hUp) BullGreen else BearRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // All-time return pill
                        Surface(
                            color = if (isAllTimeUp) BullGreen.copy(alpha = 0.12f) else BearRed.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text("Unrealized Profit", color = TextMutedDark, fontSize = 9.sp)
                                Text(
                                    text = if (isHideBalance) "•••" else "${if (isAllTimeUp) "+" else "-"}$%,.2f (%,.1f%%)".format(abs(totalAllTimePnL), abs(totalAllTimePnLPct)),
                                    color = if (isAllTimeUp) BullGreen else BearRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons: Deposit, Trade, + Add Asset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenDeposit,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DanaGold, contentColor = Color.Black),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("portfolio_deposit_btn")
                        ) {
                            Icon(Icons.Default.Wallet, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Deposit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onNavigateToTrade("BTC/USD") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimaryDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("portfolio_trade_btn")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Trade", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onOpenAddAsset,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DanaSurfaceElevatedDark,
                                contentColor = DanaGold
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("portfolio_add_asset_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Asset", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 2. Asset Allocation Bar & Legend
        if (holdings.isNotEmpty()) {
            item {
                Surface(
                    color = DanaSurfaceDark,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PieChart, contentDescription = null, tint = DanaGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Asset Allocation",
                                    color = TextPrimaryDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${holdings.size} Assets",
                                color = TextMutedDark,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Multi-color segmented allocation bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(DanaSurfaceElevatedDark)
                        ) {
                            holdings.forEach { holding ->
                                val weight = (holding.allocationPercent / 100.0).toFloat().coerceIn(0.02f, 1f)
                                Box(
                                    modifier = Modifier
                                        .weight(weight)
                                        .fillMaxSize()
                                        .background(parseHexColor(holding.colorHex))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Legend Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(holdings, key = { it.id }) { h ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(parseHexColor(h.colorHex))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${h.symbol} %,.1f%%".format(h.allocationPercent),
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Section Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Cryptocurrency Holdings",
                    color = TextPrimaryDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Real-time Ticker",
                    color = BullGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 4. Holdings List or Empty State
        if (holdings.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(DanaSurfaceDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wallet,
                            contentDescription = null,
                            tint = DanaGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "No Crypto Holdings Tracked",
                        color = TextPrimaryDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Track your Bitcoin, Ethereum, Solana, and BNB holdings with real-time profit and loss calculations.",
                        color = TextMutedDark,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onOpenAddAsset,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DanaGold, contentColor = Color.Black)
                    ) {
                        Text("+ Add First Holding", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(holdings, key = { it.id }) { holding ->
                HoldingItemCard(
                    holding = holding,
                    isHideBalance = isHideBalance,
                    onTrade = { onNavigateToTrade("${holding.symbol}/USD") },
                    onDelete = { onDeleteHolding(holding.id) }
                )
            }
        }
    }
}

@Composable
private fun HoldingItemCard(
    holding: HoldingItem,
    isHideBalance: Boolean,
    onTrade: () -> Unit,
    onDelete: () -> Unit
) {
    val isProfit = holding.totalPnL >= 0
    val is24hUp = holding.change24h >= 0

    Surface(
        color = DanaSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("holding_card_${holding.symbol}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Coin Icon, Symbol, Name, Total Holding Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(holding.colorHex).copy(alpha = 0.2f))
                            .border(1.5.dp, parseHexColor(holding.colorHex), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = holding.symbol.take(2),
                            color = parseHexColor(holding.colorHex),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = holding.symbol,
                                color = TextPrimaryDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (is24hUp) BullGreen.copy(alpha = 0.15f) else BearRed.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "${if (is24hUp) "+" else ""}%,.2f%%".format(holding.change24h),
                                    color = if (is24hUp) BullGreen else BearRed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = holding.name,
                            color = TextMutedDark,
                            fontSize = 11.sp
                        )
                    }
                }

                // Total Value & Quantity
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isHideBalance) "••••••" else "$%,.2f".format(holding.currentValue),
                        color = TextPrimaryDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isHideBalance) "•••" else "%,.4f ${holding.symbol}".format(holding.amount),
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Row: Market Price, Avg Buy, Total Return
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DanaSurfaceElevatedDark)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Market Price", color = TextMutedDark, fontSize = 9.sp)
                    Text("$%,.2f".format(holding.currentPrice), color = TextPrimaryDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Column {
                    Text("Avg Buy Price", color = TextMutedDark, fontSize = 9.sp)
                    Text("$%,.2f".format(holding.avgBuyPrice), color = TextSecondaryDark, fontSize = 11.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Unrealized P&L", color = TextMutedDark, fontSize = 9.sp)
                    Text(
                        text = if (isHideBalance) "•••" else "${if (isProfit) "+" else "-"}$%,.2f (%,.1f%%)".format(abs(holding.totalPnL), abs(holding.totalPnLPercent)),
                        color = if (isProfit) BullGreen else BearRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Trade button & Remove holding button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Portfolio Share: ",
                        color = TextMutedDark,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "%,.1f%%".format(holding.allocationPercent),
                        color = DanaGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove Holding",
                            tint = TextMutedDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DanaGold.copy(alpha = 0.2f))
                            .clickable { onTrade() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Trade ${holding.symbol}",
                            color = DanaGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        DanaGold
    }
}
