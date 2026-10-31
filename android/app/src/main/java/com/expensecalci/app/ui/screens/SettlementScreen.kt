package com.expensecalci.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensecalci.app.model.SettlementStatus
import com.expensecalci.app.model.SettlementTransaction
import com.expensecalci.app.ui.theme.Emerald500
import com.expensecalci.app.ui.theme.Emerald600
import com.expensecalci.app.ui.theme.ExpenseRed
import com.expensecalci.app.ui.theme.IncomeGreen
import com.expensecalci.app.ui.theme.IncomeGreenSoft
import com.expensecalci.app.ui.theme.IndigoAccent
import com.expensecalci.app.ui.theme.Slate400
import com.expensecalci.app.ui.theme.Slate900
import com.expensecalci.app.ui.theme.WarningAmber
import com.expensecalci.app.ui.theme.WarningAmberSoft
import com.expensecalci.app.viewmodel.ExpenseUiState
import java.util.Locale

@Composable
fun SettlementScreen(
    uiState: ExpenseUiState,
    onMarkSettlementPaid: (String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var pendingActionTx by remember { mutableStateOf<SettlementTransaction?>(null) }

    val activeSettlements = uiState.settlements.filter { it.status != SettlementStatus.CONFIRMED }
    val historySettlements = uiState.settlements.filter { it.status == SettlementStatus.CONFIRMED }

    val displayedSettlements = if (selectedTabIndex == 0) activeSettlements else historySettlements

    val currentUser = uiState.currentUserName
    val userOwed = uiState.overview.toReceive
    val userOwes = uiState.overview.toPay

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Settlement Engine",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Optimized via Greedy Min-Cash-Flow to minimize transactions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Net Position Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Your Settlement Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (userOwed > 0.01) {
                        Text(
                            text = "You are owed ₹${String.format(Locale.getDefault(), "%,.2f", userOwed)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Emerald500
                        )
                        Text(
                            text = "Roommates owe you money. Collect when they mark paid.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA7F3D0)
                        )
                    } else if (userOwes > 0.01) {
                        Text(
                            text = "You owe ₹${String.format(Locale.getDefault(), "%,.2f", userOwes)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                        Text(
                            text = "Pay your share to keep the flat finances clear.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFECDD3)
                        )
                    } else {
                        Text(
                            text = "All Settled Up! 🎉",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Emerald500
                        )
                        Text(
                            text = "You have no outstanding debts or receivables.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                }
            }
        }

        // Tabs: Active vs Completed History
        item {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = Emerald600,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Emerald600
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "Active (${activeSettlements.size})",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "Settled (${historySettlements.size})",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        // List of Transactions
        if (displayedSettlements.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (selectedTabIndex == 0) "✅" else "📜",
                                fontSize = 36.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (selectedTabIndex == 0)
                                    "No pending settlements!"
                                else
                                    "No completed settlements yet.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (selectedTabIndex == 0)
                                    "Everyone is squared up."
                                else
                                    "Confirmed payments will show up here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        }
                    }
                }
            }
        } else {
            items(displayedSettlements, key = { it.id }) { settlement ->
                SettlementItemCard(
                    settlement = settlement,
                    currentUser = currentUser,
                    onAction = { pendingActionTx = settlement }
                )
            }
        }
    }

    // Confirmation Dialog before marking Paid/Confirmed
    pendingActionTx?.let { tx ->
        val isPending = tx.status == SettlementStatus.PENDING
        val titleText = if (isPending) "Mark as Paid?" else "Confirm Payment Received?"
        val messageText = if (isPending) {
            "Confirm that ${tx.debtor} has paid ₹${String.format(Locale.getDefault(), "%,.2f", tx.amount)} to ${tx.creditor}?"
        } else {
            "Confirm that you received ₹${String.format(Locale.getDefault(), "%,.2f", tx.amount)} from ${tx.debtor}?"
        }

        AlertDialog(
            onDismissRequest = { pendingActionTx = null },
            title = { Text(titleText, fontWeight = FontWeight.Bold) },
            text = { Text(messageText) },
            confirmButton = {
                Button(
                    onClick = {
                        onMarkSettlementPaid(tx.id)
                        pendingActionTx = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPending) Emerald600 else IndigoAccent
                    )
                ) {
                    Text(if (isPending) "Mark Paid" else "Confirm", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingActionTx = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettlementItemCard(
    settlement: SettlementTransaction,
    currentUser: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDebtor = settlement.debtor == currentUser
    val isCreditor = settlement.creditor == currentUser

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Transfer Visual: Debtor -> Arrow -> Creditor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Debtor
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isDebtor) ExpenseRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = settlement.debtor.take(1),
                                fontWeight = FontWeight.Bold,
                                color = if (isDebtor) ExpenseRed else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isDebtor) "You" else settlement.debtor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Arrow
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "pays",
                        tint = Slate400,
                        modifier = Modifier.padding(horizontal = 14.dp).size(20.dp)
                    )

                    // Creditor
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isCreditor) Emerald500.copy(alpha = 0.2f) else Emerald500),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = settlement.creditor.take(1),
                                fontWeight = FontWeight.Bold,
                                color = if (isCreditor) Emerald600 else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isCreditor) "You" else settlement.creditor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Amount
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%,.2f", settlement.amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Emerald600
                )
            }

            // Status Badge and Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (statusLabel, statusColor, statusBg) = when (settlement.status) {
                    SettlementStatus.PENDING -> Triple("PENDING", WarningAmber, WarningAmberSoft)
                    SettlementStatus.PAID -> Triple("PAID • Awaiting Conf.", IndigoAccent, IndigoAccent.copy(alpha = 0.12f))
                    SettlementStatus.CONFIRMED -> Triple("CONFIRMED", IncomeGreen, IncomeGreenSoft)
                    SettlementStatus.DISPUTED -> Triple("DISPUTED", ExpenseRed, ExpenseRed.copy(alpha = 0.12f))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (settlement.status == SettlementStatus.CONFIRMED) Icons.Default.CheckCircle else Icons.Default.PendingActions,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp).padding(end = 4.dp)
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                if (settlement.status != SettlementStatus.CONFIRMED) {
                    Button(
                        onClick = onAction,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (settlement.status == SettlementStatus.PENDING) Emerald600 else IndigoAccent
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (settlement.status == SettlementStatus.PENDING) "Mark Paid" else "Confirm Received",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
