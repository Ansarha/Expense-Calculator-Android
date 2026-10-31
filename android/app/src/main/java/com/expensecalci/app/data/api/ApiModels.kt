package com.expensecalci.app.data.api

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("status") val status: String? = null,
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null,
    @SerializedName("errors") val errors: List<Any>? = null
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
    @SerializedName("token_type") val tokenType: String? = "bearer",
    @SerializedName("user") val user: UserDto? = null
)

data class UserDto(
    val id: Int,
    val name: String,
    val email: String? = null,
    val mobile: String? = null,
    val role: String? = "user",
    val currency: String? = "INR",
    val timezone: String? = "Asia/Kolkata"
)

// Room DTOs
data class RoomDto(
    val id: Int,
    val name: String,
    val currency: String? = "INR",
    val timezone: String? = "Asia/Kolkata",
    @SerializedName("billing_start_day") val billingStartDay: Int? = 1,
    @SerializedName("billing_cycle_type") val billingCycleType: String? = "monthly",
    @SerializedName("billing_cycle_start_day") val billingCycleStartDay: Int? = 1,
    @SerializedName("billing_end_rule") val billingEndRule: String? = "LAST_DAY",
    val description: String? = null,
    @SerializedName("members_count") val membersCount: Int? = null
)

data class CreateRoomRequest(
    val name: String,
    val currency: String = "INR",
    val timezone: String = "Asia/Kolkata",
    @SerializedName("billing_start_day") val billingStartDay: Int = 1,
    @SerializedName("billing_cycle_type") val billingCycleType: String = "monthly",
    @SerializedName("billing_cycle_start_day") val billingCycleStartDay: Int = 1,
    @SerializedName("billing_end_rule") val billingEndRule: String = "LAST_DAY",
    val description: String = ""
)

data class RoomInvitationDto(
    val id: Int? = null,
    @SerializedName("room_id") val roomId: Int? = null,
    @SerializedName("invite_code") val inviteCode: String? = null,
    val code: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null
)

// Member DTOs
data class MemberDto(
    val id: Int,
    @SerializedName("room_id") val roomId: Int? = null,
    @SerializedName("user_id") val userId: Int? = null,
    val name: String,
    val mobile: String? = null,
    val role: String? = "member",
    @SerializedName("joining_date") val joiningDate: String? = null,
    @SerializedName("joined_at") val joinedAt: String? = null,
    @SerializedName("leaving_date") val leavingDate: String? = null,
    @SerializedName("left_at") val leftAt: String? = null,
    val status: String? = "active"
)

data class AddMemberRequest(
    val name: String? = null,
    @SerializedName("user_id") val userId: Int? = null,
    val mobile: String? = null,
    val role: String = "member",
    @SerializedName("joining_date") val joiningDate: String? = null,
    @SerializedName("joined_at") val joinedAt: String? = null
)

// Expense DTOs
data class ExpenseShareDto(
    val id: Int? = null,
    @SerializedName("member_id") val memberId: Int,
    @SerializedName("member_name") val memberName: String? = null,
    @SerializedName("share_amount") val shareAmount: Double? = null,
    @SerializedName("share_value") val shareValue: Double? = null,
    val percentage: Double? = null
)

data class ExpensePayerDto(
    @SerializedName("member_id") val memberId: Int,
    @SerializedName("amount_paid") val amountPaid: Double
)

data class CreateExpenseRequest(
    val title: String,
    val amount: Double,
    @SerializedName("paid_by") val paidBy: Int = 1,
    @SerializedName("payer_member_id") val payerMemberId: Int? = null,
    @SerializedName("category_id") val categoryId: Int = 1,
    @SerializedName("split_type") val splitType: String = "equal",
    @SerializedName("expense_date") val expenseDate: String,
    val description: String = "",
    val notes: String = "",
    val shares: List<ExpenseShareDto>? = null,
    val payers: List<ExpensePayerDto>? = null
)

data class ExpenseDto(
    val id: Int,
    @SerializedName("room_id") val roomId: Int? = null,
    val title: String,
    val description: String? = null,
    val amount: Double = 0.0,
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("paid_by") val paidBy: Int? = null,
    @SerializedName("payer_member_id") val payerMemberId: Int? = null,
    @SerializedName("payer_name") val payerName: String? = null,
    @SerializedName("split_type") val splitType: String = "equal",
    @SerializedName("expense_date") val expenseDate: String = "",
    val notes: String? = null,
    val shares: List<ExpenseShareDto>? = null
)

data class PaginatedExpensesDto(
    val total: Int = 0,
    val items: List<ExpenseDto> = emptyList(),
    val skip: Int = 0,
    val limit: Int = 50
)

// Balances & Settlements DTOs
data class RoomSummaryDto(
    @SerializedName("total_expenses") val totalExpenses: Double? = 0.0,
    @SerializedName("total_expense") val totalExpense: Double? = 0.0,
    @SerializedName("room_name") val roomName: String? = null,
    val month: Int = 10,
    val year: Int = 2026,
    @SerializedName("total_members") val totalMembers: Int? = 0,
    @SerializedName("active_members") val activeMembers: Int? = 0,
    @SerializedName("member_count") val memberCount: Int? = 0,
    @SerializedName("total_receivable") val totalReceivable: Double? = 0.0,
    @SerializedName("total_payable") val totalPayable: Double? = 0.0
)

data class MemberBalanceDto(
    @SerializedName("member_id") val memberId: Int,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("member_name") val memberName: String? = null,
    val name: String? = null,
    val role: String? = null,
    val status: String? = null,
    @SerializedName("total_paid") val totalPaid: Double = 0.0,
    @SerializedName("total_share") val totalShare: Double = 0.0,
    @SerializedName("net_balance") val netBalance: Double = 0.0,
    @SerializedName("balance_type") val balanceType: String? = null
)

data class SettlementDto(
    val id: Int,
    @SerializedName("room_id") val roomId: Int? = null,
    @SerializedName("debtor_id") val debtorId: Int? = null,
    @SerializedName("debtor_name") val debtorName: String? = null,
    val debtor: String? = null,
    @SerializedName("from_member_id") val fromMemberId: Int? = null,
    @SerializedName("from_name") val fromName: String? = null,
    @SerializedName("creditor_id") val creditorId: Int? = null,
    @SerializedName("creditor_name") val creditorName: String? = null,
    val creditor: String? = null,
    @SerializedName("to_member_id") val toMemberId: Int? = null,
    @SerializedName("to_name") val toName: String? = null,
    val amount: Double = 0.0,
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
    val status: String? = "STABLE",
    @SerializedName("total_income") val totalIncome: Double = 0.0,
    @SerializedName("total_expenses") val totalExpenses: Double = 0.0,
    @SerializedName("total_expense") val totalExpense: Double = 0.0,
    @SerializedName("available_balance") val availableBalance: Double = 0.0,
    @SerializedName("adjusted_balance") val adjustedBalance: Double = 0.0,
    @SerializedName("amount_payable") val amountPayable: Double = 0.0,
    @SerializedName("to_pay") val toPay: Double = 0.0,
    @SerializedName("amount_receivable") val amountReceivable: Double = 0.0,
    @SerializedName("to_receive") val toReceive: Double = 0.0,
    @SerializedName("status_description") val statusDescription: String? = null
)
