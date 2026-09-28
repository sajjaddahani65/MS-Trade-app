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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AccountEntity
import com.example.ui.theme.BullGreen
import com.example.ui.theme.DanaBorderDark
import com.example.ui.theme.DanaGold
import com.example.ui.theme.DanaGoldDark
import com.example.ui.theme.DanaSurfaceDark
import com.example.ui.theme.DanaSurfaceElevatedDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun DanaTopBar(
    currentAccountType: String,
    account: AccountEntity?,
    onSwitchAccount: (String) -> Unit,
    onDepositClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAccountMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = DanaSurfaceDark,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Logo & Brand Name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(DanaGold, DanaGoldDark)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BD",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BTC",
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "DANA",
                            color = DanaGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = "Global Trading",
                        color = TextMutedDark,
                        fontSize = 10.sp
                    )
                }
            }

            // Right: Account Type Switcher & Balance
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .testTag("account_switcher_chip")
                            .clip(RoundedCornerShape(20.dp))
                            .background(DanaSurfaceElevatedDark)
                            .border(1.dp, DanaBorderDark, RoundedCornerShape(20.dp))
                            .clickable { isAccountMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge indicating DEMO or REAL
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (currentAccountType == "DEMO") Color(0xFF1E88E5) else BullGreen
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = currentAccountType,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        val balanceText = account?.let { "$%,.2f".format(it.balance) } ?: "$0.00"
                        Text(
                            text = balanceText,
                            color = TextPrimaryDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Switch Account",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isAccountMenuExpanded,
                        onDismissRequest = { isAccountMenuExpanded = false },
                        modifier = Modifier.background(DanaSurfaceElevatedDark)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            "Demo Account",
                                            color = TextPrimaryDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "Practice with virtual funds",
                                            color = TextSecondaryDark,
                                            fontSize = 11.sp
                                        )
                                    }
                                    if (currentAccountType == "DEMO") {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = DanaGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onSwitchAccount("DEMO")
                                isAccountMenuExpanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            "Real Account",
                                            color = TextPrimaryDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "Live funds & instant deposits",
                                            color = TextSecondaryDark,
                                            fontSize = 11.sp
                                        )
                                    }
                                    if (currentAccountType == "REAL") {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = DanaGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onSwitchAccount("REAL")
                                isAccountMenuExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Quick Deposit / Wallet Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(DanaGold.copy(alpha = 0.15f))
                        .clickable { onDepositClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet Deposit",
                        tint = DanaGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
