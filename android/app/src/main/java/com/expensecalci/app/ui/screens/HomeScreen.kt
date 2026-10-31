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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensecalci.app.data.api.ApiClient
import com.expensecalci.app.model.Expense
import com.expensecalci.app.ui.components.BalanceOverviewCard
import com.expensecalci.app.ui.components.ExpenseDetailSheet
import com.expensecalci.app.ui.components.ExpenseItemCard
import com.expensecalci.app.ui.navigation.Screen
import com.expensecalci.app.ui.theme.Emerald500
import com.expensecalci.app.ui.theme.Emerald600
import com.expensecalci.app.ui.theme.IndigoAccent
import com.expensecalci.app.ui.theme.Slate400
import com.expensecalci.app.ui.theme.Slate900
import com.expensecalci.app.ui.theme.WarningAmber
import com.expensecalci.app.viewmodel.ExpenseUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: ExpenseUiState,
    onNavigate: (String) -> Unit,
    onDeleteExpense: (String) -> Unit,
    onUpdateServerUrl: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    onSelectMonth: (Int, Int) -> Unit = { _, _ -> }
) {
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }
    var showServerDialog by remember { mutableStateOf(false) }
    var inputServerUrl by remember { mutableStateOf(uiState.serverUrl) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val todayStr = remember {
        SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()).format(Date())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Room Info & Server Status Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active Room Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = Emerald600,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.currentRoomName.isNotEmpty()) uiState.currentRoomName else "No Room Selected",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Server Status Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (uiState.isOnline) Emerald600.copy(alpha = 0.12f)
                            else WarningAmber.copy(alpha = 0.12f)
                        )
                        .clickable { showServerDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isOnline) Emerald600 else WarningAmber)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.isOnline) "🟢 Vercel Online" else "Offline / Local",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isOnline) Emerald600 else WarningAmber
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Server Settings",
                        tint = if (uiState.isOnline) Emerald600 else WarningAmber,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // User Greeting Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = todayStr.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = Emerald600,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (uiState.currentRoomName.isNotEmpty()) uiState.currentRoomName else "Expense Overview",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Emerald600),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Hero Balance Overview Card (Monthly Total Spent)
        item {
            BalanceOverviewCard(
                overview = uiState.overview,
                selectedMonth = uiState.selectedMonth,
                selectedYear = uiState.selectedYear,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                monthWiseSummaries = uiState.monthlySpendSummaries,
                onSelectMonth = onSelectMonth
            )
        }

        // Quick Action Shortcuts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Add Expense",
                    color = Emerald600,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.AddExpense.route) }
                )
                QuickActionButton(
                    icon = Icons.Default.Handshake,
                    label = "Settle Balances",
                    color = IndigoAccent,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Settlements.route) }
                )
            }
        }

        // Budget Utilization Strip
        item {
            val totalSpent = uiState.overview.totalExpense
            val budget = uiState.monthlyBudget
            val ratio = if (budget > 0) (totalSpent / budget).toFloat().coerceIn(0f, 1f) else 0f
            val percentUsed = (ratio * 100).toInt()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = Emerald600,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Monthly Budget Utilization",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "$percentUsed% spent",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (ratio > 0.85f) Color(0xFFEF4444) else Emerald600
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (ratio > 0.85f) Color(0xFFEF4444) else Emerald500,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "₹${String.format(Locale.getDefault(), "%,.0f", totalSpent)} spent",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                        Text(
                            text = "₹${String.format(Locale.getDefault(), "%,.0f", budget - totalSpent)} remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Recent Expenses Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Expenses",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigate(Screen.Expenses.route) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View All (${uiState.expenses.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = Emerald600,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "See All",
                        tint = Emerald600,
                        modifier = Modifier.size(16.dp).padding(start = 2.dp)
                    )
                }
            }
        }

        // Recent items (up to 4)
        if (uiState.expenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No expenses recorded yet. Tap + to add one!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400
                        )
                    }
                }
            }
        } else {
            items(uiState.expenses.take(4), key = { it.id }) { expense ->
                ExpenseItemCard(
                    expense = expense,
                    onClick = { selectedExpense = expense },
                    onDelete = { onDeleteExpense(expense.id) }
                )
            }
        }
    }

    // Modal Bottom Sheet for Expense Details
    selectedExpense?.let { exp ->
        ExpenseDetailSheet(
            expense = exp,
            sheetState = sheetState,
            onDismiss = { selectedExpense = null },
            onDelete = { id ->
                onDeleteExpense(id)
                selectedExpense = null
            }
        )
    }

    // Server Configuration Dialog
    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = {
                Text(
                    text = "Backend API Connection",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Connect to your cloud or local FastAPI backend (Default: Vercel Cloud).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )

                    OutlinedTextField(
                        value = inputServerUrl,
                        onValueChange = { inputServerUrl = it },
                        label = { Text("Base URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { inputServerUrl = ApiClient.VERCEL_CLOUD_URL },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("☁️ Vercel Cloud (explicitto.vercel.app)", color = Emerald600, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { inputServerUrl = ApiClient.EMULATOR_URL },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("📱 Emulator", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall)
                            }
                            Button(
                                onClick = { inputServerUrl = ApiClient.LOCALHOST_URL },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("💻 Localhost", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Text(
                        text = "Status: ${if (uiState.isOnline) "🟢 Connected to API" else "🟡 Offline / Using Local Mode"}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateServerUrl(inputServerUrl)
                        onRefresh()
                        showServerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("Save & Sync", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.10f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
