package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AccountEntity
import com.example.data.local.DailyRewardEntity
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
fun ProfileScreen(
    currentAccountType: String,
    account: AccountEntity?,
    rewardState: DailyRewardEntity?,
    onSwitchAccount: (String) -> Unit,
    onOpenDeposit: () -> Unit,
    onOpenWithdraw: () -> Unit,
    onResetDemoBalance: () -> Unit,
    onClaimCheckIn: () -> Unit,
    onToggleMining: () -> Unit,
    modifier: Modifier = Modifier
) {
    var oneClickTrading by remember { mutableStateOf(true) }
    var soundHaptics by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DanaBgDark),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. User Header
        item {
            Surface(
                color = DanaSurfaceDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(DanaGold, DanaGoldDark))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "BD",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Trader #88492",
                                color = TextPrimaryDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DanaGold.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("VIP 2", color = DanaGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = BullGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "KYC Verified • Identity Protected",
                                color = TextMutedDark,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Wallets & Balances Card
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
                    Text(
                        text = "Trading Assets & Balance",
                        color = TextMutedDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$%,.2f".format(account?.balance ?: 0.0),
                                color = TextPrimaryDark,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Active in $currentAccountType Account",
                                color = DanaGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Toggle Demo vs Real
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DanaSurfaceElevatedDark)
                                .border(1.dp, DanaBorderDark, RoundedCornerShape(8.dp))
                                .clickable {
                                    val nextType = if (currentAccountType == "DEMO") "REAL" else "DEMO"
                                    onSwitchAccount(nextType)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Switch to ${if (currentAccountType == "DEMO") "REAL" else "DEMO"}",
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary Assets: BDC Coins & Coupons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DanaSurfaceElevatedDark)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = DanaGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("BDC Coins", color = TextMutedDark, fontSize = 10.sp)
                                Text("${account?.bdcCoins ?: 2500}", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Loss Coupons", color = TextMutedDark, fontSize = 10.sp)
                                Text("${rewardState?.couponsCount ?: 2} Active", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Deposit & Withdraw Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenDeposit,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DanaGold, contentColor = Color.Black),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("profile_deposit_button")
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Deposit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenWithdraw,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimaryDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("profile_withdraw_button")
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Withdraw", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    if (currentAccountType == "DEMO") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onResetDemoBalance,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DanaSurfaceElevatedDark,
                                contentColor = TextSecondaryDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("reset_demo_balance_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Virtual Balance ($10,000)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 2b. Official Deposit & Payment Channels (EasyPaisa, JazzCash, BNB Smart Chain)
        item {
            val context = LocalContext.current
            val clipboardManager = LocalClipboardManager.current

            Surface(
                color = DanaSurfaceDark,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Official Payment Channels",
                            color = TextPrimaryDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BullGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Active & Verified", color = BullGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // EasyPaisa Channel
                    PaymentChannelRow(
                        title = "EasyPaisa",
                        tag = "03153943904",
                        subtitle = "Instant Mobile Deposit • Pakistan",
                        accentColor = Color(0xFF00C853),
                        onCopy = {
                            clipboardManager.setText(AnnotatedString("03153943904"))
                            Toast.makeText(context, "Copied EasyPaisa: 03153943904", Toast.LENGTH_SHORT).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // JazzCash Channel
                    PaymentChannelRow(
                        title = "JazzCash",
                        tag = "03267009751",
                        subtitle = "Instant Mobile Deposit • Pakistan",
                        accentColor = Color(0xFFFF6D00),
                        onCopy = {
                            clipboardManager.setText(AnnotatedString("03267009751"))
                            Toast.makeText(context, "Copied JazzCash: 03267009751", Toast.LENGTH_SHORT).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // BNB Smart Chain Channel
                    PaymentChannelRow(
                        title = "BNB Smart Chain (BEP-20)",
                        tag = "0x0EBC03eBcE80A4b5f165D387975cb04758B8b52D",
                        subtitle = "BEP-20 Official Treasury Address",
                        accentColor = DanaGold,
                        isCompactAddress = true,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString("0x0EBC03eBcE80A4b5f165D387975cb04758B8b52D"))
                            Toast.makeText(context, "Copied BNB Smart Chain Address", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // 3. Daily Rewards & Check-in
        item {
            Surface(
                color = DanaSurfaceDark,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("7-Day Check-in Streak", color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Earn trading coupons & BDC bonus coins", color = TextMutedDark, fontSize = 11.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DanaGold.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("Day ${rewardState?.streakDays ?: 1}", color = DanaGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (day in 1..7) {
                            val isClaimed = day <= (rewardState?.streakDays ?: 1)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isClaimed) BullGreen else DanaSurfaceElevatedDark)
                                        .border(1.dp, if (isClaimed) BullGreen else DanaBorderDark, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isClaimed) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text("+$day", color = TextMutedDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("D$day", color = TextMutedDark, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onClaimCheckIn,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DanaGold, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Text("Claim Today's Bonus (+100 BDC)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 4. BDC Cloud Mining Simulator
        item {
            val isMining = rewardState?.isMiningActive ?: false

            Surface(
                color = DanaSurfaceDark,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isMining) BullGreen.copy(alpha = 0.2f) else DanaSurfaceElevatedDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = if (isMining) BullGreen else TextMutedDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "BDC Cloud Mining",
                                color = TextPrimaryDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isMining) "Active: 145.8 MH/s • Generating Coins" else "Paused • Tap to resume",
                                color = if (isMining) BullGreen else TextMutedDark,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = onToggleMining,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMining) BearRed.copy(alpha = 0.2f) else BullGreen,
                            contentColor = if (isMining) BearRed else Color.Black
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(if (isMining) "Stop" else "Start", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // 5. Trading Preferences & Security Switches
        item {
            Surface(
                color = DanaSurfaceDark,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DanaBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Trading Settings", color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("1-Click Instant Execution", color = TextPrimaryDark, fontSize = 13.sp)
                            Text("Bypass order confirmation modal for rapid entry", color = TextMutedDark, fontSize = 11.sp)
                        }
                        Switch(
                            checked = oneClickTrading,
                            onCheckedChange = { oneClickTrading = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = DanaGold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Haptic Feedback & Sound", color = TextPrimaryDark, fontSize = 13.sp)
                            Text("Vibrate on order fill, TP and SL triggers", color = TextMutedDark, fontSize = 11.sp)
                        }
                        Switch(
                            checked = soundHaptics,
                            onCheckedChange = { soundHaptics = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = DanaGold)
                        )
                    }
                }
            }
        }

        // 6. Regulatory & Support Footer
        item {
            Surface(
                color = DanaSurfaceElevatedDark.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = DanaGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("24/7 Professional Trading Support", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Regulated by the UK Financial Conduct Authority and FSC Mauritius (License No. GB22200578). All trading accounts include Negative Balance Protection.",
                        color = TextMutedDark,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentChannelRow(
    title: String,
    tag: String,
    subtitle: String,
    accentColor: Color,
    isCompactAddress: Boolean = false,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DanaSurfaceElevatedDark)
            .border(1.dp, DanaBorderDark, RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = tag,
                color = DanaGold,
                fontSize = if (isCompactAddress) 11.sp else 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(subtitle, color = TextMutedDark, fontSize = 10.sp)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(accentColor.copy(alpha = 0.2f))
                .clickable { onCopy() }
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = accentColor, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("COPY", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

