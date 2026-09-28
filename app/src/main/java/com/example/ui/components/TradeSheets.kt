package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TradePositionEntity
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
import com.example.ui.theme.DanaBorderDark
import com.example.ui.theme.DanaGold
import com.example.ui.theme.DanaSurfaceDark
import com.example.ui.theme.DanaSurfaceElevatedDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

data class SupportedPaymentMethod(
    val id: String,
    val name: String,
    val detailValue: String,
    val networkOrType: String,
    val color: Color,
    val instructions: String,
    val accountTitle: String
)

val OfficialPaymentMethods = listOf(
    SupportedPaymentMethod(
        id = "EASYPAISA",
        name = "EasyPaisa",
        detailValue = "03153943904",
        networkOrType = "PK Mobile Wallet",
        color = Color(0xFF00C853), // EasyPaisa Green
        instructions = "Transfer via EasyPaisa App or *786# to official account. Instant credit on verification.",
        accountTitle = "BTC Dana Official (EasyPaisa)"
    ),
    SupportedPaymentMethod(
        id = "JAZZCASH",
        name = "JazzCash",
        detailValue = "03267009751",
        networkOrType = "PK Mobile Wallet",
        color = Color(0xFFFF6D00), // JazzCash Orange
        instructions = "Transfer via JazzCash App or *786# to official account. Instant credit on verification.",
        accountTitle = "BTC Dana Official (JazzCash)"
    ),
    SupportedPaymentMethod(
        id = "BNB_CHAIN",
        name = "BNB Smart Chain",
        detailValue = "0x0EBC03eBcE80A4b5f165D387975cb04758B8b52D",
        networkOrType = "BEP-20 Network",
        color = Color(0xFFF0B90B), // BNB Gold
        instructions = "Send BNB or USDT (BEP-20) to this official wallet address. 12 block confirmations required.",
        accountTitle = "BTC Dana Official Treasury (BEP-20)"
    ),
    SupportedPaymentMethod(
        id = "USDT_TRC20",
        name = "USDT (TRC-20)",
        detailValue = "TXk984kLmqZb85V1hE7840q2aBc984d",
        networkOrType = "TRC-20 Network",
        color = Color(0xFF26A69A),
        instructions = "Send TRC-20 USDT for fast global settlement.",
        accountTitle = "BTC Dana Global Settlement"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositBottomSheet(
    accountType: String,
    onDismiss: () -> Unit,
    onDeposit: (Double, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedMethod by remember { mutableStateOf(OfficialPaymentMethods[0]) }
    var amount by remember { mutableDoubleStateOf(100.0) }
    var customAmountText by remember { mutableStateOf("100") }
    var trxIdText by remember { mutableStateOf("") }

    val quickAmounts = listOf(20.0, 50.0, 100.0, 250.0, 500.0, 1000.0)

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Deposit Funds",
                        color = TextPrimaryDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Credited to $accountType Account",
                        color = DanaGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Payment Method Selector Chips
            Text(
                text = "Select Payment Method",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(OfficialPaymentMethods, key = { it.id }) { method ->
                    val isSelected = method.id == selectedMethod.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) method.color.copy(alpha = 0.2f) else DanaSurfaceElevatedDark)
                            .border(
                                1.5.dp,
                                if (isSelected) method.color else DanaBorderDark,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedMethod = method }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = method.name,
                                color = if (isSelected) method.color else TextPrimaryDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = method.networkOrType,
                                color = TextMutedDark,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Payment Account Details Card (EasyPaisa / JazzCash / BNB Smart Chain)
            Surface(
                color = DanaSurfaceElevatedDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, selectedMethod.color.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(selectedMethod.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedMethod.name,
                                color = TextPrimaryDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(selectedMethod.color.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = selectedMethod.networkOrType,
                                color = selectedMethod.color,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (selectedMethod.id == "BNB_CHAIN") "Wallet Address:" else "Account Number:",
                        color = TextMutedDark,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Detail Value + Copy Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DanaSurfaceDark)
                            .border(1.dp, DanaBorderDark, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedMethod.detailValue,
                            color = DanaGold,
                            fontSize = if (selectedMethod.id == "BNB_CHAIN") 11.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DanaGold.copy(alpha = 0.2f))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(selectedMethod.detailValue))
                                    Toast.makeText(context, "Copied ${selectedMethod.name} to clipboard", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = DanaGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("COPY", color = DanaGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Title: ${selectedMethod.accountTitle}",
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = selectedMethod.instructions,
                        color = TextMutedDark,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Deposit Amount Selector
            Text("Deposit Amount (USD)", color = TextSecondaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickAmounts.take(4).forEach { q ->
                    val isSel = amount == q
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) DanaGold.copy(alpha = 0.2f) else DanaSurfaceElevatedDark)
                            .border(1.dp, if (isSel) DanaGold else DanaBorderDark, RoundedCornerShape(6.dp))
                            .clickable {
                                amount = q
                                customAmountText = q.toInt().toString()
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$${q.toInt()}",
                            color = if (isSel) DanaGold else TextSecondaryDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customAmountText,
                onValueChange = {
                    customAmountText = it
                    it.toDoubleOrNull()?.let { parsed -> amount = parsed }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

            Spacer(modifier = Modifier.height(8.dp))

            // Optional TRX / TxID
            OutlinedTextField(
                value = trxIdText,
                onValueChange = { trxIdText = it },
                label = { Text("Transaction ID / TRX Hash (Optional)", color = TextMutedDark, fontSize = 11.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark,
                    focusedBorderColor = DanaGold,
                    unfocusedBorderColor = DanaBorderDark
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val methodDescription = "${selectedMethod.name} (${selectedMethod.detailValue})"
                    onDeposit(amount, methodDescription)
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DanaGold, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("confirm_deposit_button")
            ) {
                Text(
                    text = "Deposit $%,.2f via ${selectedMethod.name}".format(amount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawBottomSheet(
    accountType: String,
    balance: Double,
    onDismiss: () -> Unit,
    onWithdraw: (Double, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedMethod by remember { mutableStateOf(OfficialPaymentMethods[0]) }
    var destinationAccount by remember { mutableStateOf(selectedMethod.detailValue) }
    var withdrawAmountText by remember { mutableStateOf("100") }

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
                Column {
                    Text(
                        text = "Withdraw Funds",
                        color = TextPrimaryDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Available: $%,.2f USD".format(balance), color = BullGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Select Payout Channel", color = TextSecondaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(OfficialPaymentMethods, key = { it.id }) { method ->
                    val isSel = method.id == selectedMethod.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) method.color.copy(alpha = 0.2f) else DanaSurfaceElevatedDark)
                            .border(1.5.dp, if (isSel) method.color else DanaBorderDark, RoundedCornerShape(8.dp))
                            .clickable {
                                selectedMethod = method
                                destinationAccount = method.detailValue
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = method.name,
                            color = if (isSel) method.color else TextPrimaryDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (selectedMethod.id == "BNB_CHAIN") "BNB Smart Chain (BEP-20) Destination:" else "${selectedMethod.name} Account Number:",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = destinationAccount,
                onValueChange = { destinationAccount = it },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark,
                    focusedBorderColor = selectedMethod.color,
                    unfocusedBorderColor = DanaBorderDark
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Amount to Withdraw (USD)", color = TextSecondaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = withdrawAmountText,
                onValueChange = { withdrawAmountText = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

            Spacer(modifier = Modifier.height(16.dp))

            val reqAmount = withdrawAmountText.toDoubleOrNull() ?: 0.0
            val isValid = reqAmount > 0 && reqAmount <= balance

            Button(
                onClick = {
                    val methodDesc = "${selectedMethod.name} ($destinationAccount)"
                    onWithdraw(reqAmount, methodDesc)
                },
                enabled = isValid,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DanaGold, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("confirm_withdraw_button")
            ) {
                Text(
                    text = "Submit Withdrawal of $%,.2f".format(reqAmount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (!isValid && reqAmount > balance) {
                Text(
                    text = "Requested amount exceeds available balance.",
                    color = BearRed,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTpSlBottomSheet(
    position: TradePositionEntity,
    onDismiss: () -> Unit,
    onSave: (Long, Double?, Double?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var tpText by remember { mutableStateOf(position.takeProfit?.let { "%,.2f".format(it).replace(",", "") } ?: "") }
    var slText by remember { mutableStateOf(position.stopLoss?.let { "%,.2f".format(it).replace(",", "") } ?: "") }

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
                    text = "Edit TP / SL (${position.symbol})",
                    color = TextPrimaryDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                }
            }

            Text(
                text = "${position.direction} ${position.lotSize} Lots @ Open: $%,.2f".format(position.openPrice),
                color = TextSecondaryDark,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Take Profit Target Price ($)", color = BullGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = tpText,
                onValueChange = { tpText = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                placeholder = { Text("No TP set", color = TextMutedDark) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark,
                    focusedBorderColor = BullGreen,
                    unfocusedBorderColor = DanaBorderDark
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Stop Loss Target Price ($)", color = BearRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = slText,
                onValueChange = { slText = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                placeholder = { Text("No SL set", color = TextMutedDark) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark,
                    focusedBorderColor = BearRed,
                    unfocusedBorderColor = DanaBorderDark
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val tp = tpText.toDoubleOrNull()
                    val sl = slText.toDoubleOrNull()
                    onSave(position.id, tp, sl)
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DanaGold, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Save TP / SL", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
