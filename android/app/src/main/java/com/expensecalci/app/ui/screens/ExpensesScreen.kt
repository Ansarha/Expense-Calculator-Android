package com.expensecalci.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensecalci.app.model.Expense
import com.expensecalci.app.model.ExpenseCategory
import com.expensecalci.app.ui.components.ExpenseDetailSheet
import com.expensecalci.app.ui.components.ExpenseItemCard
import com.expensecalci.app.ui.theme.Emerald500
import com.expensecalci.app.ui.theme.Emerald600
import com.expensecalci.app.ui.theme.Slate400
import com.expensecalci.app.viewmodel.ExpenseUiState
import java.util.Locale

enum class ExpenseSortOrder(val label: String) {
    NEWEST("Newest First"),
    AMOUNT_HIGH("Amount: High to Low"),
    AMOUNT_LOW("Amount: Low to High")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    uiState: ExpenseUiState,
    onCategoryFilter: (ExpenseCategory?) -> Unit,
    onDeleteExpense: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(ExpenseSortOrder.NEWEST) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    var selectedExpense by remember { mutableStateOf<Expense?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Filter by Category and Search Query
    val filteredExpenses = uiState.expenses
        .filter { expense ->
            (uiState.selectedCategory == null || expense.category == uiState.selectedCategory) &&
                    (searchQuery.isBlank() ||
                            expense.title.contains(searchQuery, ignoreCase = true) ||
                            expense.paidBy.contains(searchQuery, ignoreCase = true) ||
                            expense.notes.contains(searchQuery, ignoreCase = true) ||
                            expense.participants.any { it.contains(searchQuery, ignoreCase = true) })
        }
        .let { list ->
            when (sortOrder) {
                ExpenseSortOrder.NEWEST -> list
                ExpenseSortOrder.AMOUNT_HIGH -> list.sortedByDescending { it.amount }
                ExpenseSortOrder.AMOUNT_LOW -> list.sortedBy { it.amount }
            }
        }

    val totalFilteredSum = filteredExpenses.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Screen Header & Sort Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "All Expenses",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Track, search, and manage room expenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }

            Box {
                IconButton(
                    onClick = { sortMenuExpanded = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Sort Options",
                        tint = Emerald600
                    )
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    ExpenseSortOrder.values().forEach { order ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = order.label,
                                    fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                    color = if (sortOrder == order) Emerald600 else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                sortOrder = order
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search title, payer, notes...", color = Slate400) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Slate400
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = Slate400
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Emerald600,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        )

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.padding(start = 16.dp))

            FilterChip(
                selected = uiState.selectedCategory == null,
                onClick = { onCategoryFilter(null) },
                label = { Text("All (${uiState.expenses.size})", fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Emerald600,
                    selectedLabelColor = Color.White
                )
            )

            ExpenseCategory.values().forEach { category ->
                val count = uiState.expenses.count { it.category == category }
                FilterChip(
                    selected = uiState.selectedCategory == category,
                    onClick = {
                        if (uiState.selectedCategory == category) onCategoryFilter(null)
                        else onCategoryFilter(category)
                    },
                    label = { Text("${category.displayName} ($count)", fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald600,
                        selectedLabelColor = Color.White
                    )
                )
            }

            Box(modifier = Modifier.padding(end = 16.dp))
        }

        // Summary Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredExpenses.size} ${if (filteredExpenses.size == 1) "expense" else "expenses"}",
                style = MaterialTheme.typography.labelMedium,
                color = Slate400,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Total: ₹${String.format(Locale.getDefault(), "%,.2f", totalFilteredSum)}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Emerald600
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredExpenses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp),
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
                                    text = "🔍",
                                    fontSize = 36.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No matching expenses",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try adjusting your search query or filters.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredExpenses, key = { it.id }) { expense ->
                    ExpenseItemCard(
                        expense = expense,
                        onClick = { selectedExpense = expense },
                        onDelete = { onDeleteExpense(expense.id) }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Details
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
}
