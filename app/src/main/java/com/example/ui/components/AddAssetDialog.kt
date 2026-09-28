package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketInstrument
import com.example.ui.theme.DanaBorderDark
import com.example.ui.theme.DanaGold
import com.example.ui.theme.DanaSurfaceDark
import com.example.ui.theme.DanaSurfaceElevatedDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

data class SelectableCoin(
    val symbol: String,
    val name: String,
    val colorHex: String,
    val defaultPrice: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetDialog(
    instruments: List<MarketInstrument>,
    onDismiss: () -> Unit,
    onAddHolding: (symbol: String, name: String, amount: Double, buyPrice: Double, colorHex: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val availableCoins = remember(instruments) {
        listOf(
            SelectableCoin("BTC", "Bitcoin", "#F0B90B", instruments.find { it.symbol == "BTC/USD" }?.price ?: 89250.0),
            SelectableCoin("ETH", "Ethereum", "#627EEA", instruments.find { it.symbol == "ETH/USD" }?.price ?: 3420.0),
            SelectableCoin("SOL", "Solana", "#00FFA3", instruments.find { it.symbol == "SOL/USD" }?.price ?: 188.0),
            SelectableCoin("BNB", "BNB Smart Chain", "#F3BA2F", instruments.find { it.symbol == "BNB/USD" }?.price ?: 598.0),
            SelectableCoin("DOGE", "Dogecoin", "#C2A633", instruments.find { it.symbol == "DOGE/USD" }?.price ?: 0.22),
            SelectableCoin("XAU", "Gold (Tokenized)", "#E6B800", instruments.find { it.symbol == "XAU/USD" }?.price ?: 2728.0)
        )
    }

    var selectedCoin by remember { mutableStateOf(availableCoins[0]) }
    var amountText by remember { mutableStateOf("0.5") }
    var buyPriceText by remember { mutableStateOf("%.2f".format(selectedCoin.defaultPrice)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DanaSurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Crypto Asset",
                    color = TextPrimaryDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Select Cryptocurrency", color = TextSecondaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableCoins, key = { it.symbol }) { coin ->
                    val isSel = coin.symbol == selectedCoin.symbol
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) DanaGold.copy(alpha = 0.2f) else DanaSurfaceElevatedDark)
                            .border(1.5.dp, if (isSel) DanaGold else DanaBorderDark, RoundedCornerShape(8.dp))
                            .clickable {
                                selectedCoin = coin
                                buyPriceText = "%.2f".format(coin.defaultPrice)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(coin.colorHex))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(coin.symbol.take(1), color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = coin.symbol,
                                color = if (isSel) DanaGold else TextPrimaryDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Quantity Held (${selectedCoin.symbol})", color = TextSecondaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
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

            Spacer(modifier = Modifier.height(14.dp))

            Text("Average Purchase Price (USD)", color = TextSecondaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = buyPriceText,
                onValueChange = { buyPriceText = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                prefix = { Text("$ ", color = TextSecondaryDark) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark,
                    focusedBorderColor = DanaGold,
                    unfocusedBorderColor = DanaBorderDark
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            val amountVal = amountText.toDoubleOrNull() ?: 0.0
            val priceVal = buyPriceText.toDoubleOrNull() ?: 0.0
            val totalInvestment = amountVal * priceVal

            Text(
                text = "Total Investment: $%,.2f USD".format(totalInvestment),
                color = DanaGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (amountVal > 0 && priceVal > 0) {
                        onAddHolding(
                            selectedCoin.symbol,
                            selectedCoin.name,
                            amountVal,
                            priceVal,
                            selectedCoin.colorHex
                        )
                    }
                },
                enabled = amountVal > 0 && priceVal > 0,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DanaGold, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Add to Portfolio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
