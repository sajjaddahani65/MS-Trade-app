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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.example.data.model.EconomicEvent
import com.example.data.model.ImpactLevel
import com.example.data.model.MarketNews
import com.example.data.model.SignalDirection
import com.example.data.model.TradingSignal
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
import com.example.ui.theme.WarningOrange

@Composable
fun SignalsScreen(
    signals: List<TradingSignal>,
    calendarEvents: List<EconomicEvent>,
    news: List<MarketNews>,
    onApplySignal: (TradingSignal) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Signals, 1: Calendar, 2: News

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DanaBgDark),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Tab switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DanaSurfaceDark)
                    .padding(4.dp)
            ) {
                listOf("Signals", "Calendar", "News").forEachIndexed { index, name ->
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
                            text = name,
                            color = if (isSelected) Color.Black else TextSecondaryDark,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        when (selectedSubTab) {
            0 -> {
                // Signals List
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = DanaGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Daily Analyst Trading Signals",
                            color = TextPrimaryDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(signals, key = { it.id }) { sig ->
                    SignalCard(signal = sig, onApply = { onApplySignal(sig) })
                }
            }

            1 -> {
                // Economic Calendar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Major Financial Economic Events",
                            color = TextPrimaryDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(calendarEvents, key = { it.id }) { event ->
                    CalendarEventCard(event = event)
                }
            }

            2 -> {
                // Financial News
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Newspaper, contentDescription = null, tint = DanaGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Latest Market Intelligence",
                            color = TextPrimaryDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(news, key = { it.id }) { item ->
                    NewsCard(news = item)
                }
            }
        }
    }
}

@Composable
private fun SignalCard(
    signal: TradingSignal,
    onApply: () -> Unit
) {
    val isBuy = signal.direction == SignalDirection.BUY

    Surface(
        color = DanaSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("signal_card_${signal.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Symbol, Direction, Confidence, Time
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
                            text = signal.direction.name,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = signal.symbol,
                        color = TextPrimaryDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DanaGold.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${signal.confidence}% Confidence",
                            color = DanaGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = signal.timeAgo,
                        color = TextMutedDark,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing Targets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DanaSurfaceElevatedDark)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Entry Price", color = TextMutedDark, fontSize = 10.sp)
                    Text("$%,.2f".format(signal.entryPrice), color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Target TP1", color = TextMutedDark, fontSize = 10.sp)
                    Text("$%,.2f".format(signal.targetPrice1), color = BullGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Target TP2", color = TextMutedDark, fontSize = 10.sp)
                    Text("$%,.2f".format(signal.targetPrice2), color = BullGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Stop Loss", color = TextMutedDark, fontSize = 10.sp)
                    Text("$%,.2f".format(signal.stopLoss), color = BearRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rationale
            Text(
                text = signal.rationale,
                color = TextSecondaryDark,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Apply Button
            Button(
                onClick = onApply,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DanaGold,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("apply_signal_btn_${signal.id}")
            ) {
                Text(
                    text = "Apply Signal to Trade",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun CalendarEventCard(event: EconomicEvent) {
    Surface(
        color = DanaSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(event.countryFlag, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(event.currency, color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(event.time, color = TextMutedDark, fontSize = 11.sp)
                }

                // Impact Tag
                val impactColor = when (event.impact) {
                    ImpactLevel.HIGH -> BearRed
                    ImpactLevel.MEDIUM -> WarningOrange
                    ImpactLevel.LOW -> TextMutedDark
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(impactColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${event.impact.name} IMPACT",
                        color = impactColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = event.title,
                color = TextPrimaryDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(DanaSurfaceElevatedDark)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text("Actual: ", color = TextMutedDark, fontSize = 11.sp)
                    Text(event.actual ?: "Pending", color = if (event.actual != null) BullGreen else TextMutedDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Row {
                    Text("Forecast: ", color = TextMutedDark, fontSize = 11.sp)
                    Text(event.forecast, color = TextPrimaryDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Row {
                    Text("Previous: ", color = TextMutedDark, fontSize = 11.sp)
                    Text(event.previous, color = TextSecondaryDark, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun NewsCard(news: MarketNews) {
    Surface(
        color = DanaSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${news.source} • ${news.timeAgo}",
                    color = DanaGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${news.readMinutes} min read",
                    color = TextMutedDark,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = news.title,
                color = TextPrimaryDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = news.summary,
                color = TextSecondaryDark,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
