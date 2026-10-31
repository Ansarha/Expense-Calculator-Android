package com.expensecalci.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.expensecalci.app.ui.screens.AddExpenseScreen
import com.expensecalci.app.ui.screens.AnalyticsScreen
import com.expensecalci.app.ui.screens.ExpensesScreen
import com.expensecalci.app.ui.screens.HomeScreen
import com.expensecalci.app.ui.screens.LoginScreen
import com.expensecalci.app.ui.screens.SettlementScreen
import com.expensecalci.app.ui.theme.Emerald600
import com.expensecalci.app.ui.theme.Slate400
import com.expensecalci.app.viewmodel.ExpenseViewModel

@Composable
fun AppNavigation(
    viewModel: ExpenseViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.syncWithBackend()
    }

    val showBottomBar = currentRoute != Screen.AddExpense.route

    if (!uiState.isAuthenticated) {
        LoginScreen(
            uiState = uiState,
            onLogin = { emailOrMobile, password -> viewModel.login(emailOrMobile, password) },
            onRegister = { name, email, password, mobile -> viewModel.register(name, email, password, mobile) },
            onQuickDemoLogin = { viewModel.quickDemoLogin() },
            onUpdateServerUrl = { url -> viewModel.updateServerUrl(url) }
        )
    } else {
        Scaffold(
            bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val pendingCount = uiState.settlements.count { it.status == com.expensecalci.app.model.SettlementStatus.PENDING }

                    BottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                if (screen == Screen.Settlements && pendingCount > 0) {
                                    androidx.compose.material3.BadgedBox(
                                        badge = {
                                            androidx.compose.material3.Badge(
                                                containerColor = com.expensecalci.app.ui.theme.ExpenseRed,
                                                contentColor = Color.White
                                            ) {
                                                Text("$pendingCount")
                                            }
                                        }
                                    ) {
                                        screen.icon?.let {
                                            Icon(imageVector = it, contentDescription = screen.title)
                                        }
                                    }
                                } else {
                                    screen.icon?.let {
                                        Icon(imageVector = it, contentDescription = screen.title)
                                    }
                                }
                            },
                            label = { Text(screen.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Emerald600,
                                selectedTextColor = Emerald600,
                                indicatorColor = Emerald600.copy(alpha = 0.15f),
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddExpense.route) },
                    containerColor = Emerald600,
                    contentColor = Color.White
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    uiState = uiState,
                    onNavigate = { route -> navController.navigate(route) },
                    onDeleteExpense = { id -> viewModel.deleteExpense(id) },
                    onUpdateServerUrl = { url -> viewModel.updateServerUrl(url) },
                    onRefresh = { viewModel.syncWithBackend() },
                    onPreviousMonth = { viewModel.selectPreviousMonth() },
                    onNextMonth = { viewModel.selectNextMonth() },
                    onSelectMonth = { month, year -> viewModel.selectMonth(month, year) },
                    onLogout = { allDevices -> viewModel.logout(allDevices) }
                )
            }
            composable(Screen.Expenses.route) {
                ExpensesScreen(
                    uiState = uiState,
                    onCategoryFilter = { cat -> viewModel.filterByCategory(cat) },
                    onDeleteExpense = { id -> viewModel.deleteExpense(id) }
                )
            }
            composable(Screen.Settlements.route) {
                SettlementScreen(
                    uiState = uiState,
                    onMarkSettlementPaid = { id -> viewModel.markSettlementPaid(id) }
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(uiState = uiState)
            }
            composable(Screen.AddExpense.route) {
                AddExpenseScreen(
                    uiState = uiState,
                    onNavigateBack = { navController.popBackStack() },
                    onAddMember = { name -> viewModel.addMember(name) },
                    onSaveExpense = { title, amount, category, paidBy, splitType, participants, notes ->
                        viewModel.addExpense(title, amount, category, paidBy, splitType, participants, notes)
                    }
                )
            }
        }
    }
}
}
