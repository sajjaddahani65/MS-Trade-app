package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InstrumentCategory
import com.example.data.model.MarketInstrument
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
import com.example.ui.theme.DanaBgDark
import com.example.ui.theme.DanaBorderDark
import com.example.ui.theme.DanaGold
import com.example.ui.theme.DanaGoldDark
import com.example.ui.theme.DanaSurfaceDark
import com.example.ui.theme.DanaSurfaceElevatedDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun MarketsScreen(
    instruments: List<MarketInstrument>,
    selectedCategory: InstrumentCategory,
    searchQuery: String,
    onCategorySelected: (InstrumentCategory) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onInstrumentClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenSignals: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val filteredInstruments = instruments.filter { item ->
        val matchesCategory = when (selectedCategory) {
            InstrumentCategory.ALL -> true
            else -> item.category == selectedCategory
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            item.symbol.contains(searchQuery, ignoreCase = true) ||
                    item.name.contains(searchQuery, ignoreCase = true)
        }
        matchesCategory && matchesSearch
    }

    val topMovers = instruments.take(4)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DanaBgDark),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Search Input
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = {
                        Text(
                            text = "Search crypto, forex, gold...",
                            color = TextMutedDark,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextMutedDark,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DanaSurfaceDark,
                        unfocusedContainerColor = DanaSurfaceDark,
                        focusedBorderColor = DanaGold,
                        unfocusedBorderColor = DanaBorderDark,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("markets_search_input")
                )
            }
        }

        // Top Movers Banner
        if (searchQuery.isEmpty()) {
            item {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = DanaGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Market Highlights",
                                color = TextPrimaryDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DanaSurfaceElevatedDark)
                                .border(1.dp, DanaBorderDark, RoundedCornerShape(6.dp))
                                .clickable { onOpenSignals() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⚡ Signals & News",
                                color = DanaGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(topMovers, key = { it.symbol }) { item ->
                            MarketHighlightCard(
                                instrument = item,
                                onClick = { onInstrumentClick(item.symbol) }
                            )
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(InstrumentCategory.entries, key = { it.name }) { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .testTag("category_chip_${cat.name}")
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) DanaGold else DanaSurfaceDark)
                            .border(
                                1.dp,
                                if (isSelected) DanaGold else DanaBorderDark,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { onCategorySelected(cat) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat.displayName,
                            color = if (isSelected) Color.Black else TextSecondaryDark,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Instruments Table Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Name / Vol",
                    color = TextMutedDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    text = "Last Price",
                    color = TextMutedDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = "24h Chg%",
                    color = TextMutedDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Instrument List
        if (filteredInstruments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching instruments found",
                        color = TextSecondaryDark,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(filteredInstruments, key = { it.symbol }) { item ->
                InstrumentRowItem(
                    instrument = item,
                    onClick = { onInstrumentClick(item.symbol) },
                    onToggleFavorite = { onToggleFavorite(item.symbol) }
                )
            }
        }
    }
}

@Composable
private fun MarketHighlightCard(
    instrument: MarketInstrument,
    onClick: () -> Unit
) {
    val isUp = instrument.change24h >= 0

    Surface(
        color = DanaSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
        modifier = Modifier
            .width(135.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = instrument.symbol,
                color = TextPrimaryDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$%,.${instrument.decimals}f".format(instrument.price),
                color = TextPrimaryDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isUp) BullGreen.copy(alpha = 0.15f) else BearRed.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${if (isUp) "+" else ""}%,.2f%%".format(instrument.change24h),
                        color = if (isUp) BullGreen else BearRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun InstrumentRowItem(
    instrument: MarketInstrument,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val isUp = instrument.change24h >= 0

    // Price change flash animation
    val priceColor by animateColorAsState(
        targetValue = if (instrument.price > instrument.prevPrice) BullGreen
        else if (instrument.price < instrument.prevPrice) BearRed
        else TextPrimaryDark,
        animationSpec = tween(durationMillis = 400),
        label = "price_flash"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Star & Name
        Row(
            modifier = Modifier.weight(1.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DanaSurfaceElevatedDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = instrument.symbol.take(3),
                    color = DanaGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = instrument.symbol,
                        color = TextPrimaryDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(DanaBorderDark)
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${instrument.defaultLeverage}x",
                            color = TextMutedDark,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Vol: ${instrument.volume24h}",
                    color = TextMutedDark,
                    fontSize = 11.sp
                )
            }
        }

        // Live Price
        Column(
            modifier = Modifier.weight(1.2f)
        ) {
            Text(
                text = "$%,.${instrument.decimals}f".format(instrument.price),
                color = priceColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Spread ${instrument.spread}",
                color = TextMutedDark,
                fontSize = 10.sp
            )
        }

        // 24h Change Box
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isUp) BullGreen else BearRed)
                .padding(vertical = 7.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${if (isUp) "+" else ""}%,.2f%%".format(instrument.change24h),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
