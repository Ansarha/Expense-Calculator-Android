package com.expensecalci.app.data.repository

import com.expensecalci.app.data.api.AddMemberRequest
import com.expensecalci.app.data.api.ApiClient
import com.expensecalci.app.data.api.CreateExpenseRequest
import com.expensecalci.app.data.api.CreateRoomRequest
import com.expensecalci.app.data.api.ExpenseApiService
import com.expensecalci.app.data.api.ExpenseDto
import com.expensecalci.app.data.api.LoginRequest
import com.expensecalci.app.data.api.MemberDto
import com.expensecalci.app.data.api.RegisterRequest
import com.expensecalci.app.data.api.RoomDto
import com.expensecalci.app.data.api.SettlementDto
import com.expensecalci.app.data.api.StabilityScoreDto
import com.expensecalci.app.data.api.TokenResponse
import com.expensecalci.app.model.Expense
import com.expensecalci.app.model.ExpenseCategory
import com.expensecalci.app.model.Member
import com.expensecalci.app.model.SettlementStatus
import com.expensecalci.app.model.SettlementTransaction
import com.expensecalci.app.model.SplitType
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepository(
    private val customApiService: ExpenseApiService? = null
) {
    private val apiService: ExpenseApiService
        get() = customApiService ?: ApiClient.getService()

    suspend fun checkServerHealth(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.healthCheck()
            if (response.isSuccessful) return@withContext true
            val fallback = apiService.healthCheckFallback()
            fallback.isSuccessful
        } catch (e: Exception) {
            try {
                val fallback = apiService.healthCheckFallback()
                fallback.isSuccessful
            } catch (e2: Exception) {
                false
            }
        }
    }

    suspend fun ensureAuthenticated(
        email: String = "ansarha7456@gmail.com",
        password: String = "Password@123",
        name: String = "Ansarha",
        mobile: String = "9876543210"
    ): Result<TokenResponse> = withContext(Dispatchers.IO) {
        if (!ApiClient.authToken.isNullOrBlank()) {
            return@withContext Result.success(TokenResponse(accessToken = ApiClient.authToken!!))
        }
        try {
            // Attempt login
            val loginRes = apiService.login(LoginRequest(email = email, password = password))
            if (loginRes.isSuccessful && loginRes.body()?.data != null) {
                val tokenResp = loginRes.body()!!.data!!
                ApiClient.authToken = tokenResp.accessToken
                return@withContext Result.success(tokenResp)
            }

            // Fallback: register if account doesn't exist
            val regRes = apiService.register(
                RegisterRequest(
                    name = name,
                    email = email,
                    password = password,
                    mobile = mobile
                )
            )
            if (regRes.isSuccessful && regRes.body()?.data != null) {
                val tokenResp = regRes.body()!!.data!!
                ApiClient.authToken = tokenResp.accessToken
                return@withContext Result.success(tokenResp)
            }

            Result.failure(Exception(loginRes.message() ?: "Authentication failed"))
        } catch (e: Exception) {
            Result.failure(e)
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

    suspend fun createRoom(name: String, description: String = ""): Result<RoomDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createRoom(CreateRoomRequest(name = name, description = description))
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to create room"))
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

    suspend fun addMember(roomId: String, name: String, mobile: String? = null): Result<MemberDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.addMember(roomId, AddMemberRequest(name = name, mobile = mobile))
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to add member"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRoomExpenses(roomId: String, month: Int? = null, year: Int? = null): Result<List<Expense>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRoomExpenses(roomId, month, year)
            if (response.isSuccessful && response.body()?.data != null) {
                val dataElem = response.body()!!.data!!
                val gson = Gson()
                val list = mutableListOf<ExpenseDto>()

                if (dataElem.isJsonArray) {
                    val array = dataElem.asJsonArray
                    array.forEach { elem ->
                        try {
                            list.add(gson.fromJson(elem, ExpenseDto::class.java))
                        } catch (_: Exception) {}
                    }
                } else if (dataElem.isJsonObject) {
                    val obj = dataElem.asJsonObject
                    if (obj.has("items") && obj.get("items").isJsonArray) {
                        obj.getAsJsonArray("items").forEach { elem ->
                            try {
                                list.add(gson.fromJson(elem, ExpenseDto::class.java))
                            } catch (_: Exception) {}
                        }
                    }
                }

                val expenses = list.map { mapDtoToExpense(it) }
                Result.success(expenses)
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

    suspend fun recalculateSettlements(roomId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.recalculateSettlements(roomId)
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSettlements(roomId: String, month: Int? = null, year: Int? = null): Result<List<SettlementTransaction>> = withContext(Dispatchers.IO) {
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

    suspend fun getStabilityScore(month: Int? = null, year: Int? = null): Result<StabilityScoreDto> = withContext(Dispatchers.IO) {
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
            "daily_proration", "prorated_days" -> SplitType.OCCUPANCY_DAILY
            "exact", "custom" -> SplitType.CUSTOM
            else -> SplitType.EQUAL
        }

        val participants = dto.shares?.map { it.memberName ?: it.memberId.toString() } ?: emptyList()

        return Expense(
            id = dto.id.toString(),
            title = dto.title,
            amount = dto.amount,
            category = category,
            paidBy = dto.payerName ?: "Member ${dto.paidBy ?: dto.payerMemberId ?: 1}",
            splitType = splitType,
            participants = participants,
            date = dto.expenseDate.ifBlank { "2026-10-31" },
            notes = dto.description ?: dto.notes ?: ""
        )
    }

    private fun mapDtoToSettlement(dto: SettlementDto): SettlementTransaction {
        val debtor = dto.fromName ?: dto.debtorName ?: dto.debtor ?: "Member ${dto.fromMemberId ?: dto.debtorId ?: 1}"
        val creditor = dto.toName ?: dto.creditorName ?: dto.creditor ?: "Member ${dto.toMemberId ?: dto.creditorId ?: 2}"
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
            date = dto.createdAt?.take(10) ?: "2026-10-31"
        )
    }
}
