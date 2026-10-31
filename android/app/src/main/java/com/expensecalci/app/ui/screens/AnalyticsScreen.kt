package com.expensecalci.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensecalci.app.model.ExpenseCategory
import com.expensecalci.app.ui.components.getCategoryMeta
import com.expensecalci.app.ui.theme.Emerald500
import com.expensecalci.app.ui.theme.Emerald600
import com.expensecalci.app.ui.theme.IndigoAccent
import com.expensecalci.app.ui.theme.Slate400
import com.expensecalci.app.ui.theme.Slate900
import com.expensecalci.app.viewmodel.ExpenseUiState
import java.util.Locale

@Composable
fun AnalyticsScreen(
    uiState: ExpenseUiState
) {
    val totalExpense = uiState.overview.totalExpense
    val categoryTotals = ExpenseCategory.values().map { cat ->
        val matchingExpenses = uiState.expenses.filter { it.category == cat }
        val sum = matchingExpenses.sumOf { it.amount }
        val count = matchingExpenses.size
        val percentage = if (totalExpense > 0) (sum / totalExpense) * 100 else 0.0
        Triple(cat, sum to count, percentage)
    }.filter { it.second.first > 0 }.sortedByDescending { it.second.first }

    // Roommate spending totals
    val memberSpending = uiState.members.map { member ->
        val spent = uiState.expenses.filter { it.paidBy == member.name }.sumOf { it.amount }
        val pct = if (totalExpense > 0) (spent / totalExpense) * 100 else 0.0
        Triple(member, spent, pct)
    }.sortedByDescending { it.second }

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
                    text = "Financial Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Spending distribution and member contribution insights",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Budget Utilization Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Budget Utilization",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                        val budgetRatio = if (uiState.monthlyBudget > 0) (totalExpense / uiState.monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f
                        val percentUsed = (budgetRatio * 100).toInt()

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (budgetRatio > 0.85f) Color(0xFF7F1D1D) else Color(0xFF064E3B))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$percentUsed% Used",
                                color = if (budgetRatio > 0.85f) Color(0xFFFCA5A5) else Emerald500,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.0f", totalExpense)} / ₹${String.format(Locale.getDefault(), "%,.0f", uiState.monthlyBudget)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val budgetRatio = if (uiState.monthlyBudget > 0) (totalExpense / uiState.monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f
                    LinearProgressIndicator(
                        progress = { budgetRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (budgetRatio > 0.85f) Color(0xFFEF4444) else Emerald500,
                        trackColor = Color(0xFF334155)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Available", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                            Text(
                                "₹${String.format(Locale.getDefault(), "%,.0f", uiState.monthlyBudget - totalExpense)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Emerald500
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Expenses", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                            Text(
                                "${uiState.expenses.size} entries",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Segmented Visual Proportion Bar
        if (categoryTotals.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Expense Distribution",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Visual Proportional Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            categoryTotals.forEach { (cat, _, percentage) ->
                                val (_, color) = getCategoryMeta(cat)
                                val weight = (percentage / 100.0).toFloat().coerceAtLeast(0.02f)
                                Box(
                                    modifier = Modifier
                                        .weight(weight)
                                        .fillMaxSize()
                                        .background(color)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            categoryTotals.take(3).forEach { (cat, _, percentage) ->
                                val (_, color) = getCategoryMeta(cat)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${cat.displayName.take(10)} (${String.format(Locale.getDefault(), "%.0f", percentage)}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.expenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
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
                                text = "📊",
                                fontSize = 36.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Analytics Available",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add expenses to view category breakdowns and member contributions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        }
                    }
                }
            }
        } else {
            // Month-Wise Spending Breakdown Card
            if (uiState.monthlySpendSummaries.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Month-Wise Spending",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val maxSpend = uiState.monthlySpendSummaries.maxOfOrNull { it.totalAmount }?.coerceAtLeast(1.0) ?: 1.0

                            uiState.monthlySpendSummaries.forEach { summary ->
                                val ratio = (summary.totalAmount / maxSpend).toFloat().coerceIn(0f, 1f)
                                val isCurrent = summary.month == uiState.selectedMonth && summary.year == uiState.selectedYear
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = summary.label,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isCurrent) Emerald600 else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isCurrent) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Emerald600.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "Active",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Emerald600,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "₹${String.format(Locale.getDefault(), "%,.2f", summary.totalAmount)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) Emerald600 else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    LinearProgressIndicator(
                                        progress = { ratio },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (isCurrent) Emerald600 else IndigoAccent,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Category Breakdown Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Category Breakdown",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        categoryTotals.forEach { (cat, data, percentage) ->
                            val (sum, count) = data
                            val (icon, color) = getCategoryMeta(cat)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(color.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = color,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.size(10.dp))
                                        Column {
                                            Text(
                                                text = cat.displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "$count ${if (count == 1) "bill" else "bills"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Slate400,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${String.format(Locale.getDefault(), "%,.0f", sum)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${String.format(Locale.getDefault(), "%.1f", percentage)}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = color,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = { (percentage / 100.0).toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = color,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Roommate Contribution Card
            if (memberSpending.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Paid by Roommates",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            memberSpending.forEach { (member, amount, pct) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Emerald600),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = member.initial,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = member.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${String.format(Locale.getDefault(), "%.1f", pct)}% of total",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Slate400
                                            )
                                        }
                                    }

                                    Text(
                                        text = "₹${String.format(Locale.getDefault(), "%,.2f", amount)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald600
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
