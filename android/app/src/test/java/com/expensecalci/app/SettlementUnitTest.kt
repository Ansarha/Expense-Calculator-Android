package com.expensecalci.app

import com.expensecalci.app.model.Expense
import com.expensecalci.app.model.ExpenseCategory
import com.expensecalci.app.model.SplitType
import com.expensecalci.app.viewmodel.ExpenseViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettlementUnitTest {

    @Test
    fun testInitialEmptyState() {
        val viewModel = ExpenseViewModel()
        val state = viewModel.uiState.value

        // Without dummy data, state starts clean
        assertEquals(0, state.expenses.size)
        assertTrue(state.settlements.isEmpty())
        assertEquals(0.0, state.overview.totalExpense, 0.01)
        assertEquals(50000.0, state.overview.availableBalance, 0.01)
    }

    @Test
    fun testAddExpenseRecalculatesTotalsAndSettlements() {
        val viewModel = ExpenseViewModel()
        viewModel.setCurrentUserName("Alice")
        viewModel.addMember("Alice")
        viewModel.addMember("Bob")

        viewModel.addExpense(
            title = "Dinner",
            amount = 1000.0,
            category = ExpenseCategory.FOOD,
            paidBy = "Alice",
            splitType = SplitType.EQUAL,
            participants = listOf("Alice", "Bob"),
            notes = "Test"
        )

        val state = viewModel.uiState.value
        assertEquals(1, state.expenses.size)
        assertEquals(1000.0, state.overview.totalExpense, 0.01)
        // Alice paid 1000, Bob owes 500
        assertEquals(500.0, state.overview.toReceive, 0.01)
        assertEquals(0.0, state.overview.toPay, 0.01)

        // Bob should have a settlement transaction to Alice
        assertEquals(1, state.settlements.size)
        val settlement = state.settlements[0]
        assertEquals("Bob", settlement.debtor)
        assertEquals("Alice", settlement.creditor)
        assertEquals(500.0, settlement.amount, 0.01)
    }

    @Test
    fun testMultipleExpensesMinCashFlowSettlement() {
        val viewModel = ExpenseViewModel()
        viewModel.setCurrentUserName("Alice")
        viewModel.addMember("Alice")
        viewModel.addMember("Bob")
        viewModel.addMember("Charlie")

        // Alice pays 3000 for Alice, Bob, Charlie (1000 each)
        viewModel.addExpense(
            title = "Groceries",
            amount = 3000.0,
            category = ExpenseCategory.GROCERIES,
            paidBy = "Alice",
            splitType = SplitType.EQUAL,
            participants = listOf("Alice", "Bob", "Charlie"),
            notes = "Test"
        )

        // Bob pays 1500 for Alice, Bob, Charlie (500 each)
        viewModel.addExpense(
            title = "Internet",
            amount = 1500.0,
            category = ExpenseCategory.UTILITIES,
            paidBy = "Bob",
            splitType = SplitType.EQUAL,
            participants = listOf("Alice", "Bob", "Charlie"),
            notes = "Test"
        )

        val state = viewModel.uiState.value
        assertEquals(4500.0, state.overview.totalExpense, 0.01)

        // Net calculations:
        // Alice: paid 3000, share 1500 -> net +1500
        // Bob: paid 1500, share 1500 -> net 0
        // Charlie: paid 0, share 1500 -> net -1500
        // Min cash flow should produce 1 settlement: Charlie -> Alice 1500
        assertEquals(1, state.settlements.size)
        val s = state.settlements[0]
        assertEquals("Charlie", s.debtor)
        assertEquals("Alice", s.creditor)
        assertEquals(1500.0, s.amount, 0.01)
    }
}
