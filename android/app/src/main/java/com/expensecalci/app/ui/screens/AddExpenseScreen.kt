package com.expensecalci.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.expensecalci.app.model.ExpenseCategory
import com.expensecalci.app.model.SplitType
import com.expensecalci.app.ui.components.getCategoryMeta
import com.expensecalci.app.ui.theme.Emerald500
import com.expensecalci.app.ui.theme.Emerald600
import com.expensecalci.app.ui.theme.Slate400
import com.expensecalci.app.viewmodel.ExpenseUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    uiState: ExpenseUiState,
    onNavigateBack: () -> Unit,
    onAddMember: (String) -> Unit = {},
    onSaveExpense: (
        title: String,
        amount: Double,
        category: ExpenseCategory,
        paidBy: String,
        splitType: SplitType,
        participants: List<String>,
        notes: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var paidBy by remember { mutableStateOf(uiState.members.firstOrNull()?.name ?: uiState.currentUserName) }
    var selectedSplitType by remember { mutableStateOf(SplitType.EQUAL) }
    val selectedParticipants = remember {
        mutableStateListOf<String>().apply {
            addAll(uiState.members.map { it.name })
            if (isEmpty()) {
                add(uiState.currentUserName)
            }
        }
    }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var newMemberName by remember { mutableStateOf("") }

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val perPersonShare = if (selectedParticipants.isNotEmpty() && parsedAmount > 0.0) {
        parsedAmount / selectedParticipants.size
    } else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Record Expense",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Amount Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Enter Amount",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate400,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                amountText = it
                                errorMessage = null
                            }
                        },
                        placeholder = { Text("0.00", style = MaterialTheme.typography.headlineMedium) },
                        textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        leadingIcon = {
                            Text(
                                "₹",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Emerald600,
                                modifier = Modifier.padding(start = 14.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald600,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Increment Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(100, 500, 1000, 2000).forEach { inc ->
                            SuggestionChip(
                                onClick = {
                                    val current = amountText.toDoubleOrNull() ?: 0.0
                                    val updated = current + inc
                                    amountText = String.format(Locale.US, "%.0f", updated)
                                    errorMessage = null
                                },
                                label = {
                                    Text(
                                        "+₹$inc",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Emerald600.copy(alpha = 0.08f),
                                    labelColor = Emerald600
                                ),
                                border = null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Live Per-Person Split Indicator
                    if (perPersonShare > 0.0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Emerald600.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "💡 Split equally: ₹${String.format(Locale.getDefault(), "%,.2f", perPersonShare)} / person across ${selectedParticipants.size} people",
                                style = MaterialTheme.typography.labelMedium,
                                color = Emerald600,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Description / Title
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    errorMessage = null
                },
                label = { Text("Description / Title") },
                placeholder = { Text("e.g. Monthly Grocery Restock") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald600
                )
            )

            // Category Selection
            Column {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpenseCategory.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        val (icon, color) = getCategoryMeta(cat)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else color,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald600,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            val availableMemberNames = remember(uiState.members, uiState.currentUserName) {
                val list = uiState.members.map { it.name }.toMutableList()
                if (!list.contains(uiState.currentUserName)) {
                    list.add(0, uiState.currentUserName)
                }
                list
            }

            // Paid By Selection
            Column {
                Text(
                    text = "Paid By",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableMemberNames.forEach { memberName ->
                        val isSelected = paidBy == memberName
                        FilterChip(
                            selected = isSelected,
                            onClick = { paidBy = memberName },
                            label = { Text(memberName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else Emerald600),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = memberName.take(1).uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Emerald600 else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald600,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    FilterChip(
                        selected = false,
                        onClick = { showAddMemberDialog = true },
                        label = { Text("+ Add Person") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Split Mode
            Column {
                Text(
                    text = "Split Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SplitType.values().forEach { split ->
                        val isSelected = selectedSplitType == split
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSplitType = split },
                            label = { Text(split.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald600,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Split Among (Participants) with Quick Actions
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Split Among (${selectedParticipants.size}/${availableMemberNames.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row {
                        TextButton(
                            onClick = {
                                selectedParticipants.clear()
                                selectedParticipants.addAll(availableMemberNames)
                            }
                        ) {
                            Text("All", color = Emerald600, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableMemberNames.forEach { memberName ->
                        val isSelected = selectedParticipants.contains(memberName)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    if (selectedParticipants.size > 1) {
                                        selectedParticipants.remove(memberName)
                                    }
                                } else {
                                    selectedParticipants.add(memberName)
                                }
                            },
                            label = { Text(memberName) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald600.copy(alpha = 0.15f),
                                selectedLabelColor = Emerald600
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    FilterChip(
                        selected = false,
                        onClick = { showAddMemberDialog = true },
                        label = { Text("+ Add Person") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                placeholder = { Text("Additional billing reference, invoice #, etc.") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald600
                )
            )

            // Error message if any
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter an expense description."
                    } else if (parsedAmount <= 0.0) {
                        errorMessage = "Please enter an amount greater than 0."
                    } else if (selectedParticipants.isEmpty()) {
                        errorMessage = "Please select at least one participant."
                    } else {
                        onSaveExpense(
                            title.trim(),
                            parsedAmount,
                            selectedCategory,
                            paidBy,
                            selectedSplitType,
                            selectedParticipants.toList(),
                            notes.trim()
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald600
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Save Expense",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showAddMemberDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddMemberDialog = false
                newMemberName = ""
            },
            title = { Text("Add Roommate / Member") },
            text = {
                OutlinedTextField(
                    value = newMemberName,
                    onValueChange = { newMemberName = it },
                    label = { Text("Name") },
                    placeholder = { Text("e.g. John") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newMemberName.trim()
                        if (name.isNotBlank()) {
                            onAddMember(name)
                            if (!selectedParticipants.contains(name)) {
                                selectedParticipants.add(name)
                            }
                            if (paidBy.isBlank() || paidBy == "You") {
                                paidBy = name
                            }
                            showAddMemberDialog = false
                            newMemberName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddMemberDialog = false
                        newMemberName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
