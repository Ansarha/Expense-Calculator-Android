package com.expensecalci.app.data.repository

import com.expensecalci.app.data.api.ApiClient
import com.expensecalci.app.data.api.CreateExpenseRequest
import com.expensecalci.app.data.api.ExpenseApiService
import com.expensecalci.app.data.api.ExpenseDto
import com.expensecalci.app.data.api.MemberDto
import com.expensecalci.app.data.api.RoomDto
import com.expensecalci.app.data.api.SettlementDto
import com.expensecalci.app.data.api.StabilityScoreDto
import com.expensecalci.app.model.Expense
import com.expensecalci.app.model.ExpenseCategory
import com.expensecalci.app.model.Member
import com.expensecalci.app.model.SettlementStatus
import com.expensecalci.app.model.SettlementTransaction
import com.expensecalci.app.model.SplitType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepository(
    private val apiService: ExpenseApiService = ApiClient.getService()
) {

    suspend fun checkServerHealth(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.healthCheck()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getRooms(): Result<List<RoomDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRooms()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch rooms"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRoomMembers(roomId: String): Result<List<Member>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRoomMembers(roomId)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { dto ->
                    Member(
                        id = dto.id.toString(),
                        name = dto.name,
                        initial = dto.name.take(1).uppercase()
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch members"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRoomExpenses(roomId: String, month: Int, year: Int): Result<List<Expense>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRoomExpenses(roomId, month, year)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { dto ->
                    mapDtoToExpense(dto)
                }
                Result.success(list)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch expenses"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createExpense(roomId: String, request: CreateExpenseRequest): Result<ExpenseDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createExpense(roomId, request)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to create expense"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(roomId: String, expenseId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteExpense(roomId, expenseId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to delete expense"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSettlements(roomId: String, month: Int, year: Int): Result<List<SettlementTransaction>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSettlements(roomId, month, year)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { dto ->
                    mapDtoToSettlement(dto)
                }
                Result.success(list)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch settlements"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markSettlementPaid(roomId: String, settlementId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.markSettlementPaid(roomId, settlementId)
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmSettlement(roomId: String, settlementId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.confirmSettlement(roomId, settlementId)
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStabilityScore(month: Int, year: Int): Result<StabilityScoreDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getStabilityScore(month, year)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch stability"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapDtoToExpense(dto: ExpenseDto): Expense {
        val category = when (dto.categoryId) {
            1 -> ExpenseCategory.FOOD
            2 -> ExpenseCategory.UTILITIES
            3 -> ExpenseCategory.UTILITIES
            4 -> ExpenseCategory.RENT
            5 -> ExpenseCategory.GROCERIES
            6 -> ExpenseCategory.ENTERTAINMENT
            7 -> ExpenseCategory.TRANSPORT
            else -> ExpenseCategory.MISCELLANEOUS
        }

        val splitType = when (dto.splitType.lowercase()) {
            "percentage" -> SplitType.PERCENTAGE
            "prorated_days" -> SplitType.OCCUPANCY_DAILY
            "exact", "custom" -> SplitType.CUSTOM
            else -> SplitType.EQUAL
        }

        val participants = dto.shares?.map { it.memberId.toString() } ?: emptyList()

        return Expense(
            id = dto.id.toString(),
            title = dto.title,
            amount = dto.amount,
            category = category,
            paidBy = dto.payerName ?: "Member ${dto.payerMemberId ?: 1}",
            splitType = splitType,
            participants = participants,
            date = dto.expenseDate,
            notes = dto.notes ?: ""
        )
    }

    private fun mapDtoToSettlement(dto: SettlementDto): SettlementTransaction {
        val debtor = dto.debtorName ?: dto.debtor ?: "Debtor ${dto.debtorId ?: 1}"
        val creditor = dto.creditorName ?: dto.creditor ?: "Creditor ${dto.creditorId ?: 2}"
        val status = when (dto.status.lowercase()) {
            "paid" -> SettlementStatus.PAID
            "confirmed" -> SettlementStatus.CONFIRMED
            "disputed" -> SettlementStatus.DISPUTED
            else -> SettlementStatus.PENDING
        }

        return SettlementTransaction(
            id = dto.id.toString(),
            debtor = debtor,
            creditor = creditor,
            amount = dto.amount,
            status = status,
            date = dto.createdAt?.take(10) ?: "2026-10-22"
        )
    }
}
