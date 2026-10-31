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
    val monthlyExpense: Double = totalExpense,
    val availableBalance: Double,
    val adjustedBalance: Double = 0.0,
    val toPay: Double,
    val toReceive: Double,
    val status: StabilityStatus
)

data class MonthlySpendSummary(
    val month: Int,
    val year: Int,
    val label: String,
    val totalAmount: Double,
    val count: Int
)

fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> "Month $month"
    }
}

fun getShortMonthName(month: Int): String {
    return when (month) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> "M$month"
    }
}
