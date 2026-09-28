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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.TradePositionEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

@Composable
fun PositionsScreen(
    account: AccountEntity?,
    openPositions: List<TradePositionEntity>,
    pendingPositions: List<TradePositionEntity>,
    closedPositions: List<TradePositionEntity>,
    onClosePosition: (Long) -> Unit,
    onCloseAllPositions: () -> Unit,
    onCancelPendingOrder: (Long) -> Unit,
    onEditTpSl: (TradePositionEntity) -> Unit,
    onNavigateToTrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Positions, 1: Pending, 2: History

    val balance = account?.balance ?: 0.0
    val totalFloatingPnL = openPositions.sumOf { it.profit }
    val totalMarginUsed = openPositions.sumOf { it.margin }
    val equity = balance + totalFloatingPnL
    val freeMargin = max(0.0, equity - totalMarginUsed)
    val marginLevel = if (totalMarginUsed > 0) (equity / totalMarginUsed) * 100.0 else 0.0
    val isTotalUp = totalFloatingPnL >= 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DanaBgDark),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Portfolio Account Summary Card
        item {
            Surface(
                color = DanaSurfaceDark,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Floating P&L",
                                color = TextMutedDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${if (isTotalUp) "+" else "-"}$%,.2f".format(abs(totalFloatingPnL)),
                                color = if (isTotalUp) BullGreen else BearRed,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Equity (USD)",
                                color = TextMutedDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$%,.2f".format(equity),
                                color = TextPrimaryDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary metrics row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DanaSurfaceElevatedDark)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Balance", color = TextMutedDark, fontSize = 10.sp)
                            Text("$%,.2f".format(balance), color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("Margin Used", color = TextMutedDark, fontSize = 10.sp)
                            Text("$%,.2f".format(totalMarginUsed), color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("Free Margin", color = TextMutedDark, fontSize = 10.sp)
                            Text("$%,.2f".format(freeMargin), color = BullGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Margin Level", color = TextMutedDark, fontSize = 10.sp)
                            Text(
                                if (marginLevel > 0) "%,.1f%%".format(marginLevel) else "--",
                                color = if (marginLevel > 150 || marginLevel == 0.0) TextPrimaryDark else BearRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Emergency Close All Button
                    if (openPositions.size > 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onCloseAllPositions,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BearRed.copy(alpha = 0.15f),
                                contentColor = BearRed
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Text("Close All ${openPositions.size} Positions", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2. Sub-tab Selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DanaSurfaceDark)
                    .padding(4.dp)
            ) {
                listOf(
                    "Positions (${openPositions.size})",
                    "Pending (${pendingPositions.size})",
                    "History (${closedPositions.size})"
                ).forEachIndexed { index, title ->
                    val isSelected = selectedSubTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) DanaGold else Color.Transparent)
                            .clickable { selectedSubTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color.Black else TextSecondaryDark,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3. Tab Contents
        when (selectedSubTab) {
            0 -> {
                // Open Positions
                if (openPositions.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Open Positions",
                            message = "Start trading crypto, forex, and metals with real-time leverage.",
                            buttonText = "Go to Trade",
                            onAction = onNavigateToTrade
                        )
                    }
                } else {
                    items(openPositions, key = { it.id }) { pos ->
                        OpenPositionCard(
                            position = pos,
                            onClose = { onClosePosition(pos.id) },
                            onEditTpSl = { onEditTpSl(pos) }
                        )
                    }
                }
            }

            1 -> {
                // Pending Orders
                if (pendingPositions.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Pending Orders",
                            message = "Set limit or stop orders to execute automatically when market reaches your target price.",
                            buttonText = "Place Order",
                            onAction = onNavigateToTrade
                        )
                    }
                } else {
                    items(pendingPositions, key = { it.id }) { pending ->
                        PendingOrderCard(
                            order = pending,
                            onCancel = { onCancelPendingOrder(pending.id) }
                        )
                    }
                }
            }

            2 -> {
                // Closed Trade History & Performance
                item {
                    HistoryAnalyticsSummary(closedPositions = closedPositions)
                }

                if (closedPositions.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Trading History",
                            message = "Your closed positions and trading performance records will appear here.",
                            buttonText = "Start Trading",
                            onAction = onNavigateToTrade
                        )
                    }
                } else {
                    items(closedPositions, key = { it.id }) { closed ->
                        ClosedTradeCard(position = closed)
                    }
                }
            }
        }
    }
}

@Composable
private fun OpenPositionCard(
    position: TradePositionEntity,
    onClose: () -> Unit,
    onEditTpSl: () -> Unit
) {
    val isUp = position.profit >= 0
    val isBuy = position.direction == "BUY"

    Surface(
        color = DanaSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("position_card_${position.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Symbol, Buy/Sell tag, P&L
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isBuy) BullGreen else BearRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = position.direction,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = position.symbol,
                        color = TextPrimaryDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "${position.lotSize} Lots",
                        color = TextMutedDark,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "${if (isUp) "+" else "-"}$%,.2f".format(abs(position.profit)),
                    color = if (isUp) BullGreen else BearRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Open Price", color = TextMutedDark, fontSize = 10.sp)
                    Text("$%,.2f".format(position.openPrice), color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Current Price", color = TextMutedDark, fontSize = 10.sp)
                    Text("$%,.2f".format(position.currentPrice), color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Margin", color = TextMutedDark, fontSize = 10.sp)
                    Text("$%,.2f".format(position.margin), color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TP / SL", color = TextMutedDark, fontSize = 10.sp)
                    val tpStr = position.takeProfit?.let { "%,.0f".format(it) } ?: "--"
                    val slStr = position.stopLoss?.let { "%,.0f".format(it) } ?: "--"
                    Text("$tpStr / $slStr", color = DanaGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEditTpSl,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DanaGold
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit TP/SL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onClose,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BearRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("close_position_btn_${position.id}")
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Close", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PendingOrderCard(
    order: TradePositionEntity,
    onCancel: () -> Unit
) {
    val isBuy = order.direction == "BUY"

    Surface(
        color = DanaSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isBuy) BullGreen else BearRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(order.direction, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(order.symbol, color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(order.pendingType ?: "LIMIT", color = DanaGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Trigger @ $%,.2f | Lots: ${order.lotSize}".format(order.targetTriggerPrice ?: order.openPrice),
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
            }

            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BearRed),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ClosedTradeCard(
    position: TradePositionEntity
) {
    val isProfit = position.profit >= 0
    val timeFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val closeTimeStr = position.closeTime?.let { timeFormat.format(Date(it)) } ?: "--"

    Surface(
        color = DanaSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (position.direction == "BUY") BullGreen.copy(alpha = 0.2f) else BearRed.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            position.direction,
                            color = if (position.direction == "BUY") BullGreen else BearRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(position.symbol, color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${position.lotSize} lots", color = TextMutedDark, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$closeTimeStr • ${position.closeReason ?: "Closed"}",
                    color = TextMutedDark,
                    fontSize = 11.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isProfit) "+" else "-"}$%,.2f".format(abs(position.profit)),
                    color = if (isProfit) BullGreen else BearRed,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Close: $%,.2f".format(position.closePrice ?: position.openPrice),
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun HistoryAnalyticsSummary(
    closedPositions: List<TradePositionEntity>
) {
    if (closedPositions.isEmpty()) return

    val totalTrades = closedPositions.size
    val winningTrades = closedPositions.count { it.profit > 0 }
    val winRate = if (totalTrades > 0) (winningTrades.toDouble() / totalTrades) * 100.0 else 0.0
    val totalRealized = closedPositions.sumOf { it.profit }

    Surface(
        color = DanaSurfaceElevatedDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total Closed", color = TextMutedDark, fontSize = 11.sp)
                Text("$totalTrades", color = TextPrimaryDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Win Rate", color = TextMutedDark, fontSize = 11.sp)
                Text("%,.1f%%".format(winRate), color = BullGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Net Realized", color = TextMutedDark, fontSize = 11.sp)
                Text(
                    "${if (totalRealized >= 0) "+" else "-"}$%,.2f".format(abs(totalRealized)),
                    color = if (totalRealized >= 0) BullGreen else BearRed,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    message: String,
    buttonText: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(DanaSurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShowChart,
                contentDescription = null,
                tint = DanaGold,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = title,
            color = TextPrimaryDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            color = TextMutedDark,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAction,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DanaGold, contentColor = Color.Black)
        ) {
            Text(buttonText, fontWeight = FontWeight.Bold)
        }
    }
}
