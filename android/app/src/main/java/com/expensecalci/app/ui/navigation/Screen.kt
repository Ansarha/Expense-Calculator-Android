package com.expensecalci.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Expenses : Screen("expenses", "Expenses", Icons.AutoMirrored.Filled.ReceiptLong)
    object Settlements : Screen("settlements", "Settlements", Icons.Default.Handshake)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Assessment)
    object AddExpense : Screen("add_expense", "Add Expense")
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Expenses,
    Screen.Settlements,
    Screen.Analytics
)
