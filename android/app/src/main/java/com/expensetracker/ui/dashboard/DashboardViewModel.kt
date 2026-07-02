package com.expensetracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.local.entity.ExpenseEntity
import com.expensetracker.data.local.entity.PlanEntity
import com.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DashboardState(
    val plan: PlanEntity? = null,
    val expenses: List<ExpenseEntity> = emptyList(),
    val totalSpent: Double = 0.0,
    val todaySpent: Double = 0.0,
    val spendingBudget: Double = 0.0,
    val dailyAllowance: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val categoryStats: List<Pair<String, Double>> = emptyList(),
    val recentExpenses: List<ExpenseEntity> = emptyList(),
    val health: Triple<String, String, String>? = null,
    val cycleEnded: Boolean = false
)

class DashboardViewModel(app: ExpenseTrackerApp) : ViewModel() {

    private val repository = ExpenseRepository(app)
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allExpenses.collect { expenses ->
                updateState(expenses, _state.value.plan)
            }
        }
        viewModelScope.launch {
            repository.plan.collect { plan ->
                updateState(_state.value.expenses, plan)
            }
        }
        viewModelScope.launch {
            repository.syncAll()
            repository.refreshFromServer()
        }
    }

    private fun updateState(expenses: List<ExpenseEntity>, plan: PlanEntity?) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        val totalSpent = expenses.sumOf { it.amount }
        val todaySpent = expenses
            .filter { it.date.take(10) == today }
            .sumOf { it.amount }

        val categoryMap = mutableMapOf<String, Double>()
        for (e in expenses) {
            categoryMap[e.category] = (categoryMap[e.category] ?: 0.0) + e.amount
        }
        val categoryStats = categoryMap.entries
            .map { it.key to it.value }
            .sortedByDescending { it.second }

        val recentExpenses = expenses.take(5)

        var spendingBudget = 0.0
        var dailyAllowance = 0.0
        var remainingBudget = 0.0
        var cycleEnded = false
        var health: Triple<String, String, String>? = null

        if (plan != null) {
            spendingBudget = plan.pocketMoney - plan.savingsGoal
            dailyAllowance = if (plan.cycleLength > 0)
                "%.2f".format(spendingBudget / plan.cycleLength).toDouble()
            else 0.0
            remainingBudget = "%.2f".format(spendingBudget - totalSpent).toDouble()

            if (dailyAllowance > 0) {
                val diff = dailyAllowance - todaySpent
                health = when {
                    diff >= dailyAllowance * 0.2 -> Triple("Excellent", "₹${diff.toInt()} under budget", "green")
                    diff >= 0 -> Triple("Careful", "Almost reached daily limit", "yellow")
                    else -> Triple("Overspent", "₹${kotlin.math.abs(diff).toInt()} over today's limit", "red")
                }
            }
        }

        _state.value = DashboardState(
            plan = plan,
            expenses = expenses,
            totalSpent = totalSpent,
            todaySpent = todaySpent,
            spendingBudget = spendingBudget,
            dailyAllowance = dailyAllowance,
            remainingBudget = remainingBudget,
            categoryStats = categoryStats,
            recentExpenses = recentExpenses,
            health = health,
            cycleEnded = cycleEnded
        )
    }

    fun refresh() {
        viewModelScope.launch {
            repository.syncAll()
            repository.refreshFromServer()
        }
    }

    class Factory(private val app: ExpenseTrackerApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DashboardViewModel(app) as T
    }
}
