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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AccountEntity
import com.example.data.model.MarketInstrument
import com.example.ui.components.CandlestickChart
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
import kotlin.math.max

@Composable
fun TradeScreen(
    instrument: MarketInstrument?,
    allInstruments: List<MarketInstrument>,
    account: AccountEntity?,
    isCandleChart: Boolean,
    activeIndicator: String,
    chartTimeframe: String,
    orderType: String,
    orderDirection: String,
    lotSize: Double,
    leverage: Int,
    isTpEnabled: Boolean,
    tpPrice: Double?,
    isSlEnabled: Boolean,
    slPrice: Double?,
    pendingTriggerPrice: Double?,
    pendingType: String,
    onSelectInstrument: (String) -> Unit,
    onTimeframeChange: (String) -> Unit,
    onToggleChartType: () -> Unit,
    onIndicatorChange: (String) -> Unit,
    onOrderTypeChange: (String) -> Unit,
    onOrderDirectionChange: (String) -> Unit,
    onLotSizeChange: (Double) -> Unit,
    onLeverageChange: (Int) -> Unit,
    onTpEnabledChange: (Boolean, Double?) -> Unit,
    onTpPriceChange: (Double?) -> Unit,
    onSlEnabledChange: (Boolean, Double?) -> Unit,
    onSlPriceChange: (Double?) -> Unit,
    onPendingTriggerChange: (Double?) -> Unit,
    onPendingTypeChange: (String) -> Unit,
    onExecuteOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (instrument == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DanaBgDark),
            contentAlignment = Alignment.Center
        ) {
            Text("Select an instrument to trade", color = TextSecondaryDark)
        }
        return
    }

    var isSymbolDropdownOpen by remember { mutableStateOf(false) }
    val timeframes = listOf("1M", "5M", "15M", "1H", "4H", "1D")
    val quickLots = listOf(0.01, 0.05, 0.10, 0.20, 0.50, 1.00)

    val currentPrice = instrument.price
    val isUp = instrument.change24h >= 0
    val requiredMargin = (currentPrice * lotSize * instrument.contractSize) / leverage
    val balance = account?.balance ?: 0.0
    val isMarginSufficient = balance >= requiredMargin

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DanaBgDark),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Symbol Header with Quick Selector
        item {
            Surface(
                color = DanaSurfaceDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .testTag("symbol_selector_dropdown")
                                .clip(RoundedCornerShape(8.dp))
                                .background(DanaSurfaceElevatedDark)
                                .clickable { isSymbolDropdownOpen = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = instrument.symbol,
                                color = TextPrimaryDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Instrument",
                                tint = DanaGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isSymbolDropdownOpen,
                            onDismissRequest = { isSymbolDropdownOpen = false },
                            modifier = Modifier.background(DanaSurfaceElevatedDark)
                        ) {
                            allInstruments.forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                item.symbol,
                                                color = TextPrimaryDark,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(
                                                "$%,.${item.decimals}f".format(item.price),
                                                color = if (item.change24h >= 0) BullGreen else BearRed
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSelectInstrument(item.symbol)
                                        isSymbolDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$%,.${instrument.decimals}f".format(instrument.price),
                            color = if (isUp) BullGreen else BearRed,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${if (isUp) "+" else ""}%,.2f%%".format(instrument.change24h),
                                color = if (isUp) BullGreen else BearRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "H: ${instrument.high24h.toInt()} L: ${instrument.low24h.toInt()}",
                                color = TextMutedDark,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Chart Toolbar (Timeframes, Indicators, Chart Type)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DanaSurfaceDark)
                    .border(
                        androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timeframe Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    timeframes.forEach { tf ->
                        val isSelected = tf == chartTimeframe
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) DanaGold else Color.Transparent)
                                .clickable { onTimeframeChange(tf) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tf,
                                color = if (isSelected) Color.Black else TextSecondaryDark,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                // Indicators and Chart style
                Row(verticalAlignment = Alignment.CenterVertically) {
                    listOf("MA", "BOLL", "OFF").forEach { ind ->
                        val selected = (ind == activeIndicator) || (ind == "OFF" && activeIndicator == "NONE")
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (selected) DanaSurfaceElevatedDark else Color.Transparent)
                                .clickable { onIndicatorChange(if (ind == "OFF") "NONE" else ind) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = ind,
                                color = if (selected) AccentCyan else TextMutedDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onToggleChartType,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isCandleChart) Icons.Default.BarChart else Icons.Default.ShowChart,
                            contentDescription = "Toggle Chart",
                            tint = DanaGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 3. Interactive Candlestick / Line Chart
        item {
            CandlestickChart(
                candles = instrument.candles,
                isCandleChart = isCandleChart,
                activeIndicator = activeIndicator,
                decimals = instrument.decimals
            )
        }

        // 4. Order Execution Control Panel
        item {
            Surface(
                color = DanaSurfaceDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Order Type: Market vs Pending
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DanaSurfaceElevatedDark)
                            .padding(2.dp)
                    ) {
                        listOf("MARKET", "PENDING").forEach { type ->
                            val isSelected = orderType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) DanaGold else Color.Transparent)
                                    .clickable { onOrderTypeChange(type) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (type == "MARKET") "Market Order (Instant)" else "Pending Order",
                                    color = if (isSelected) Color.Black else TextSecondaryDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pending Order Options if selected
                    if (orderType == "PENDING") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("BUY_LIMIT", "SELL_LIMIT").forEach { pType ->
                                val selected = pendingType == pType
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) DanaSurfaceElevatedDark else Color.Transparent)
                                        .border(1.dp, if (selected) DanaGold else DanaBorderDark, RoundedCornerShape(6.dp))
                                        .clickable { onPendingTypeChange(pType) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pType.replace("_", " "),
                                        color = if (selected) DanaGold else TextSecondaryDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = pendingTriggerPrice?.toString() ?: "",
                            onValueChange = { onPendingTriggerChange(it.toDoubleOrNull()) },
                            label = { Text("Trigger Price ($)", color = TextMutedDark, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark,
                                focusedBorderColor = DanaGold,
                                unfocusedBorderColor = DanaBorderDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Lot Size Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lot Size",
                            color = TextSecondaryDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Stepper [-] [Value] [+]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DanaSurfaceElevatedDark)
                                .border(1.dp, DanaBorderDark, RoundedCornerShape(8.dp))
                        ) {
                            IconButton(
                                onClick = { onLotSizeChange(max(0.01, lotSize - 0.01)) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease Lot", tint = TextPrimaryDark, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "%,.2f".format(lotSize),
                                color = TextPrimaryDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            IconButton(
                                onClick = { onLotSizeChange(lotSize + 0.01) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase Lot", tint = TextPrimaryDark, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Quick Lot Chips
                    LazyRow(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(quickLots) { qLot ->
                            val isSel = lotSize == qLot
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) DanaGold.copy(alpha = 0.2f) else DanaSurfaceElevatedDark)
                                    .border(1.dp, if (isSel) DanaGold else DanaBorderDark, RoundedCornerShape(6.dp))
                                    .clickable { onLotSizeChange(qLot) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "$qLot",
                                    color = if (isSel) DanaGold else TextSecondaryDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Leverage Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Leverage",
                            color = TextSecondaryDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            instrument.leverageOptions.forEach { lev ->
                                val isSel = lev == leverage
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) DanaGold else DanaSurfaceElevatedDark)
                                        .border(1.dp, if (isSel) DanaGold else DanaBorderDark, RoundedCornerShape(6.dp))
                                        .clickable { onLeverageChange(lev) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${lev}x",
                                        color = if (isSel) Color.Black else TextSecondaryDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Take Profit (TP) Switch & Config
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Take Profit (TP)",
                                color = BullGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = isTpEnabled,
                            onCheckedChange = { checked ->
                                val defaultTp = if (orderDirection == "BUY") currentPrice * 1.02 else currentPrice * 0.98
                                onTpEnabledChange(checked, if (checked) defaultTp else null)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BullGreen,
                                uncheckedTrackColor = DanaSurfaceElevatedDark
                            )
                        )
                    }

                    if (isTpEnabled) {
                        OutlinedTextField(
                            value = tpPrice?.let { "%,.${instrument.decimals}f".format(it) } ?: "",
                            onValueChange = { onTpPriceChange(it.replace(",", "").toDoubleOrNull()) },
                            label = { Text("Target Profit Price ($)", color = TextMutedDark, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark,
                                focusedBorderColor = BullGreen,
                                unfocusedBorderColor = DanaBorderDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Stop Loss (SL) Switch & Config
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Stop Loss (SL)",
                                color = BearRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = isSlEnabled,
                            onCheckedChange = { checked ->
                                val defaultSl = if (orderDirection == "BUY") currentPrice * 0.985 else currentPrice * 1.015
                                onSlEnabledChange(checked, if (checked) defaultSl else null)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BearRed,
                                uncheckedTrackColor = DanaSurfaceElevatedDark
                            )
                        )
                    }

                    if (isSlEnabled) {
                        OutlinedTextField(
                            value = slPrice?.let { "%,.${instrument.decimals}f".format(it) } ?: "",
                            onValueChange = { onSlPriceChange(it.replace(",", "").toDoubleOrNull()) },
                            label = { Text("Stop Loss Price ($)", color = TextMutedDark, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark,
                                focusedBorderColor = BearRed,
                                unfocusedBorderColor = DanaBorderDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Margin Info Bar
                    Surface(
                        color = DanaSurfaceElevatedDark,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Required Margin",
                                    color = TextMutedDark,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "$%,.2f".format(requiredMargin),
                                    color = if (isMarginSufficient) TextPrimaryDark else BearRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Available Balance",
                                    color = TextMutedDark,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "$%,.2f".format(balance),
                                    color = BullGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dual Execution Buttons: SELL (SHORT) & BUY (LONG)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // SELL BUTTON
                        Button(
                            onClick = {
                                onOrderDirectionChange("SELL")
                                onExecuteOrder()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BearRed,
                                contentColor = Color.White
                            ),
                            enabled = isMarginSufficient,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("sell_button")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SELL / SHORT",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$%,.${instrument.decimals}f".format(currentPrice - instrument.spread / 2),
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // BUY BUTTON
                        Button(
                            onClick = {
                                onOrderDirectionChange("BUY")
                                onExecuteOrder()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BullGreen,
                                contentColor = Color.White
                            ),
                            enabled = isMarginSufficient,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("buy_button")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "BUY / LONG",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$%,.${instrument.decimals}f".format(currentPrice + instrument.spread / 2),
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    if (!isMarginSufficient) {
                        Text(
                            text = "Insufficient balance. Please deposit funds or reduce lot size.",
                            color = BearRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
