package com.expensecalci.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensecalci.app.data.api.ApiClient
import com.expensecalci.app.data.api.CreateExpenseRequest
import com.expensecalci.app.data.api.ExpenseShareDto
import com.expensecalci.app.data.api.RoomDto
import com.expensecalci.app.data.repository.ExpenseRepository
import com.expensecalci.app.model.Expense
import com.expensecalci.app.model.ExpenseCategory
import com.expensecalci.app.model.FinancialOverview
import com.expensecalci.app.model.Member
import com.expensecalci.app.model.SettlementStatus
import com.expensecalci.app.model.SettlementTransaction
import com.expensecalci.app.model.SplitType
import com.expensecalci.app.model.StabilityStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

data class ExpenseUiState(
    val members: List<Member> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val settlements: List<SettlementTransaction> = emptyList(),
    val selectedCategory: ExpenseCategory? = null,
    val monthlyBudget: Double = 50000.0,
    val currentRoomId: String = "",
    val currentRoomName: String = "",
    val rooms: List<RoomDto> = emptyList(),
    val currentUserName: String = "You",
    val isOnline: Boolean = false,
    val isLoading: Boolean = false,
    val serverUrl: String = ApiClient.baseUrl,
    val overview: FinancialOverview = FinancialOverview(
        totalExpense = 0.0,
        availableBalance = 50000.0,
        adjustedBalance = 50000.0,
        toPay = 0.0,
        toReceive = 0.0,
        status = StabilityStatus.STABLE
    )
)

class ExpenseViewModel(
    private val repository: ExpenseRepository = ExpenseRepository(),
    private val coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope
        get() = coroutineScope ?: viewModelScope

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    fun syncWithBackend() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val isHealthy = repository.checkServerHealth()

            if (isHealthy) {
                _uiState.update { it.copy(isOnline = true) }
                loadRemoteRoomData(_uiState.value.currentRoomId)
            } else {
                _uiState.update { it.copy(isOnline = false, isLoading = false) }
            }
        }
    }

    fun updateServerUrl(newUrl: String) {
        ApiClient.updateBaseUrl(newUrl)
        _uiState.update { it.copy(serverUrl = ApiClient.baseUrl) }
        syncWithBackend()
    }

    fun switchRoom(roomId: String, roomName: String) {
        _uiState.update { it.copy(currentRoomId = roomId, currentRoomName = roomName) }
        if (_uiState.value.isOnline) {
            scope.launch {
                loadRemoteRoomData(roomId)
            }
        }
    }

    private suspend fun loadRemoteRoomData(roomId: String) {
        val month = 10
        val year = 2026

        // 1. Fetch Rooms
        val roomsRes = repository.getRooms()
        val rooms = roomsRes.getOrNull() ?: _uiState.value.rooms

        // 2. Fetch Members
        val membersRes = repository.getRoomMembers(roomId)
        val members = membersRes.getOrNull() ?: _uiState.value.members

        // 3. Fetch Expenses
        val expensesRes = repository.getRoomExpenses(roomId, month, year)
        val expenses = expensesRes.getOrNull() ?: _uiState.value.expenses

        // 4. Fetch Settlements
        val settlementsRes = repository.getSettlements(roomId, month, year)
        val settlements = settlementsRes.getOrNull()

        _uiState.update { state ->
            val updated = state.copy(
                rooms = rooms,
                members = if (members.isNotEmpty()) members else state.members,
                expenses = if (expenses.isNotEmpty()) expenses else state.expenses,
                isLoading = false
            )
            val finalState = recalculateFinances(updated)
            if (settlements != null && settlements.isNotEmpty()) {
                finalState.copy(settlements = settlements)
            } else {
                finalState
            }
        }
    }

    fun addMember(name: String) {
        val clean = name.trim()
        if (clean.isBlank()) return
        _uiState.update { state ->
            if (state.members.none { it.name.equals(clean, ignoreCase = true) }) {
                val newMember = Member(id = (System.currentTimeMillis() % 100000).toString(), name = clean)
                val updatedState = state.copy(members = state.members + newMember)
                recalculateFinances(updatedState)
            } else {
                state
            }
        }
    }

    fun setCurrentUserName(name: String) {
        val clean = name.trim()
        if (clean.isBlank()) return
        _uiState.update { state ->
            val updatedState = state.copy(currentUserName = clean)
            recalculateFinances(updatedState)
        }
    }

    fun addExpense(
        title: String,
        amount: Double,
        category: ExpenseCategory,
        paidBy: String,
        splitType: SplitType,
        participants: List<String>,
        notes: String
    ) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val newExpense = Expense(
            title = title,
            amount = amount,
            category = category,
            paidBy = paidBy,
            splitType = splitType,
            participants = if (participants.isEmpty()) _uiState.value.members.map { it.name } else participants,
            date = today,
            notes = notes
        )

        _uiState.update { state ->
            val updatedList = listOf(newExpense) + state.expenses
            val updatedState = state.copy(expenses = updatedList)
            recalculateFinances(updatedState)
        }

        // Post to backend if online
        if (_uiState.value.isOnline) {
            scope.launch {
                val catId = when (category) {
                    ExpenseCategory.FOOD -> 1
                    ExpenseCategory.UTILITIES -> 2
                    ExpenseCategory.RENT -> 4
                    ExpenseCategory.GROCERIES -> 5
                    ExpenseCategory.ENTERTAINMENT -> 6
                    ExpenseCategory.TRANSPORT -> 7
                    ExpenseCategory.MISCELLANEOUS -> 8
                }

                val payerMember = _uiState.value.members.find { it.name == paidBy }
                val payerId = payerMember?.id?.toIntOrNull() ?: 1

                val shares = participants.mapNotNull { pName ->
                    val mem = _uiState.value.members.find { it.name == pName }
                    mem?.id?.toIntOrNull()?.let { mId ->
                        ExpenseShareDto(memberId = mId, shareValue = amount / participants.size)
                    }
                }

                val req = CreateExpenseRequest(
                    title = title,
                    amount = amount,
                    payerMemberId = payerId,
                    categoryId = catId,
                    splitType = when (splitType) {
                        SplitType.PERCENTAGE -> "percentage"
                        SplitType.CUSTOM -> "exact"
                        SplitType.OCCUPANCY_DAILY -> "prorated_days"
                        else -> "equal"
                    },
                    expenseDate = today,
                    notes = notes,
                    shares = shares
                )
                repository.createExpense(_uiState.value.currentRoomId, req)
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        _uiState.update { state ->
            val updatedList = state.expenses.filterNot { it.id == expenseId }
            val updatedState = state.copy(expenses = updatedList)
            recalculateFinances(updatedState)
        }

        if (_uiState.value.isOnline) {
            scope.launch {
                repository.deleteExpense(_uiState.value.currentRoomId, expenseId)
            }
        }
    }

    fun filterByCategory(category: ExpenseCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun markSettlementPaid(settlementId: String) {
        val currentTx = _uiState.value.settlements.find { it.id == settlementId }

        _uiState.update { state ->
            val updated = state.settlements.map {
                if (it.id == settlementId) {
                    val nextStatus = when (it.status) {
                        SettlementStatus.PENDING -> SettlementStatus.PAID
                        SettlementStatus.PAID -> SettlementStatus.CONFIRMED
                        else -> it.status
                    }
                    it.copy(status = nextStatus)
                } else it
            }
            state.copy(settlements = updated)
        }

        if (_uiState.value.isOnline && currentTx != null) {
            scope.launch {
                if (currentTx.status == SettlementStatus.PENDING) {
                    repository.markSettlementPaid(_uiState.value.currentRoomId, settlementId)
                } else if (currentTx.status == SettlementStatus.PAID) {
                    repository.confirmSettlement(_uiState.value.currentRoomId, settlementId)
                }
            }
        }
    }

    /**
     * SRS Section 30-32 / Section 108:
     * Calculates net balances and optimizes settlements via greedy min-cash-flow algorithm.
     */
    private fun recalculateFinances(state: ExpenseUiState): ExpenseUiState {
        val currentUser = state.currentUserName
        val totalSpent = state.expenses.sumOf { it.amount }

        // Compute net balance per member
        val netBalances = mutableMapOf<String, Double>()
        state.members.forEach { netBalances[it.name] = 0.0 }

        state.expenses.forEach { exp ->
            if (!netBalances.containsKey(exp.paidBy)) {
                netBalances[exp.paidBy] = 0.0
            }
            exp.participants.forEach { p ->
                if (!netBalances.containsKey(p)) {
                    netBalances[p] = 0.0
                }
            }

            // Creditor gets full amount
            netBalances[exp.paidBy] = (netBalances[exp.paidBy] ?: 0.0) + exp.amount

            // Debtors split the amount
            val share = if (exp.participants.isNotEmpty()) exp.amount / exp.participants.size else 0.0
            exp.participants.forEach { p ->
                netBalances[p] = (netBalances[p] ?: 0.0) - share
            }
        }

        // Greedy Min-Cash-Flow Calculation
        val debtors = mutableListOf<Pair<String, Double>>()
        val creditors = mutableListOf<Pair<String, Double>>()

        netBalances.forEach { (member, balance) ->
            if (balance < -0.01) {
                debtors.add(member to abs(balance))
            } else if (balance > 0.01) {
                creditors.add(member to balance)
            }
        }

        // Sort by amount descending
        debtors.sortByDescending { it.second }
        creditors.sortByDescending { it.second }

        val computedSettlements = mutableListOf<SettlementTransaction>()
        var dIdx = 0
        var cIdx = 0
        val dList = debtors.map { it.first to it.second }.toMutableList()
        val cList = creditors.map { it.first to it.second }.toMutableList()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        while (dIdx < dList.size && cIdx < cList.size) {
            val debtor = dList[dIdx].first
            val dAmount = dList[dIdx].second

            val creditor = cList[cIdx].first
            val cAmount = cList[cIdx].second

            val settleAmount = min(dAmount, cAmount)
            if (settleAmount > 0.01) {
                computedSettlements.add(
                    SettlementTransaction(
                        debtor = debtor,
                        creditor = creditor,
                        amount = Math.round(settleAmount * 100.0) / 100.0,
                        date = today
                    )
                )
            }

            dList[dIdx] = debtor to (dAmount - settleAmount)
            cList[cIdx] = creditor to (cAmount - settleAmount)

            if (dList[dIdx].second < 0.01) dIdx++
            if (cList[cIdx].second < 0.01) cIdx++
        }

        // Calculate current user's financial overview
        val userNet = netBalances[currentUser] ?: 0.0
        val toPay = if (userNet < 0) abs(userNet) else 0.0
        val toReceive = if (userNet > 0) userNet else 0.0

        val available = state.monthlyBudget - totalSpent
        val adjusted = available + toReceive - toPay

        val status = when {
            adjusted >= state.monthlyBudget * 0.4 -> StabilityStatus.STABLE
            adjusted >= state.monthlyBudget * 0.15 -> StabilityStatus.MODERATE
            else -> StabilityStatus.AT_RISK
        }

        val overview = FinancialOverview(
            totalExpense = totalSpent,
            availableBalance = available,
            adjustedBalance = adjusted,
            toPay = toPay,
            toReceive = toReceive,
            status = status
        )

        return state.copy(
            overview = overview,
            settlements = computedSettlements
        )
    }
}
