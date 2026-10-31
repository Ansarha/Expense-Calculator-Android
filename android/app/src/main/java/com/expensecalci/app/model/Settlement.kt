package com.expensecalci.app.model

import java.util.UUID

enum class SettlementStatus {
    PENDING,
    PAID,
    CONFIRMED,
    DISPUTED
}

data class SettlementTransaction(
    val id: String = UUID.randomUUID().toString(),
    val debtor: String,     // Who owes money
    val creditor: String,   // Who receives money
    val amount: Double,
    val status: SettlementStatus = SettlementStatus.PENDING,
    val date: String
)
