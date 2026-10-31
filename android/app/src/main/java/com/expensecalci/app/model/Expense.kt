package com.expensecalci.app.model

import java.util.UUID

enum class ExpenseCategory(val displayName: String) {
    FOOD("Food & Dining"),
    RENT("Room Rent"),
    UTILITIES("Electricity & Water"),
    GROCERIES("Groceries"),
    ENTERTAINMENT("Entertainment"),
    TRANSPORT("Transport"),
    MISCELLANEOUS("Miscellaneous")
}

enum class SplitType(val displayName: String) {
    EQUAL("Equal Split"),
    PERCENTAGE("Percentage Split"),
    CUSTOM("Custom Split"),
    OCCUPANCY_DAILY("Daily Proration")
}

data class Member(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val initial: String = name.take(1).uppercase()
)

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val paidBy: String,
    val splitType: SplitType = SplitType.EQUAL,
    val participants: List<String> = emptyList(),
    val date: String,
    val notes: String = ""
)

enum class StabilityStatus {
    STABLE,
    MODERATE,
    AT_RISK
}

data class FinancialOverview(
    val totalExpense: Double,
    val availableBalance: Double,
    val adjustedBalance: Double,
    val toPay: Double,
    val toReceive: Double,
    val status: StabilityStatus
)
