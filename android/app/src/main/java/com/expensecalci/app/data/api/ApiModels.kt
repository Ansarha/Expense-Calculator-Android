package com.expensecalci.app.data.api

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)

// Auth DTOs
data class LoginRequest(
    val email: String? = null,
    val password: String? = null,
    val mobile: String? = null,
    val otp: String? = null
)

data class RegisterRequest(
    val name: String,
    val mobile: String,
    val email: String,
    val password: String,
    val currency: String = "INR",
    val timezone: String = "Asia/Kolkata"
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = "bearer"
)

data class UserDto(
    val id: Int,
    val name: String,
    val email: String? = null,
    val mobile: String? = null,
    val currency: String? = "INR",
    val timezone: String? = "Asia/Kolkata"
)

// Room DTOs
data class RoomDto(
    val id: Int,
    val name: String,
    val currency: String? = "INR",
    @SerializedName("billing_cycle_type") val billingCycleType: String? = "monthly",
    @SerializedName("billing_cycle_start_day") val billingCycleStartDay: Int? = 1,
    val description: String? = null
)

data class CreateRoomRequest(
    val name: String,
    val currency: String = "INR",
    @SerializedName("billing_cycle_type") val billingCycleType: String = "monthly",
    @SerializedName("billing_cycle_start_day") val billingCycleStartDay: Int = 1,
    val description: String = ""
)

data class RoomInvitationDto(
    val code: String,
    @SerializedName("expires_at") val expiresAt: String? = null
)

// Member DTOs
data class MemberDto(
    val id: Int,
    val name: String,
    val mobile: String? = null,
    val role: String? = "member",
    @SerializedName("joined_at") val joinedAt: String? = null,
    @SerializedName("left_at") val leftAt: String? = null,
    val status: String? = "active"
)

data class AddMemberRequest(
    val name: String,
    val mobile: String? = null,
    val role: String = "member",
    @SerializedName("joined_at") val joinedAt: String? = null
)

// Expense DTOs
data class ExpenseShareDto(
    @SerializedName("member_id") val memberId: Int,
    @SerializedName("share_value") val shareValue: Double
)

data class ExpensePayerDto(
    @SerializedName("member_id") val memberId: Int,
    @SerializedName("amount_paid") val amountPaid: Double
)

data class CreateExpenseRequest(
    val title: String,
    val amount: Double,
    @SerializedName("payer_member_id") val payerMemberId: Int? = null,
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("split_type") val splitType: String = "equal",
    @SerializedName("expense_date") val expenseDate: String,
    val notes: String = "",
    val shares: List<ExpenseShareDto>? = null,
    val payers: List<ExpensePayerDto>? = null
)

data class ExpenseDto(
    val id: Int,
    val title: String,
    val amount: Double,
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("payer_member_id") val payerMemberId: Int? = null,
    @SerializedName("payer_name") val payerName: String? = null,
    @SerializedName("split_type") val splitType: String = "equal",
    @SerializedName("expense_date") val expenseDate: String,
    val notes: String? = null,
    val shares: List<ExpenseShareDto>? = null
)

// Balances & Settlements DTOs
data class RoomSummaryDto(
    @SerializedName("total_expense") val totalExpense: Double = 0.0,
    val month: Int = 10,
    val year: Int = 2026,
    @SerializedName("member_count") val memberCount: Int = 0
)

data class MemberBalanceDto(
    @SerializedName("member_id") val memberId: Int,
    @SerializedName("member_name") val memberName: String,
    @SerializedName("total_paid") val totalPaid: Double = 0.0,
    @SerializedName("total_share") val totalShare: Double = 0.0,
    @SerializedName("net_balance") val netBalance: Double = 0.0
)

data class SettlementDto(
    val id: Int,
    @SerializedName("debtor_id") val debtorId: Int? = null,
    @SerializedName("debtor_name") val debtorName: String? = null,
    val debtor: String? = null,
    @SerializedName("creditor_id") val creditorId: Int? = null,
    @SerializedName("creditor_name") val creditorName: String? = null,
    val creditor: String? = null,
    val amount: Double,
    val status: String = "pending",
    @SerializedName("payment_method") val paymentMethod: String? = null,
    @SerializedName("transaction_reference") val transactionReference: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class MarkPaidRequest(
    @SerializedName("payment_method") val paymentMethod: String = "upi",
    @SerializedName("transaction_reference") val transactionReference: String = "",
    val notes: String = ""
)

// Personal & Stability DTOs
data class StabilityScoreDto(
    @SerializedName("stability_status") val stabilityStatus: String? = "STABLE",
    @SerializedName("total_income") val totalIncome: Double = 0.0,
    @SerializedName("total_expense") val totalExpense: Double = 0.0,
    @SerializedName("available_balance") val availableBalance: Double = 0.0,
    @SerializedName("adjusted_balance") val adjustedBalance: Double = 0.0,
    @SerializedName("to_pay") val toPay: Double = 0.0,
    @SerializedName("to_receive") val toReceive: Double = 0.0
)
