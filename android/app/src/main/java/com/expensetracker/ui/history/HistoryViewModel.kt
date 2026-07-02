package com.expensetracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.local.entity.ExpenseEntity
import com.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class HistoryState(
    val groups: List<Pair<String, List<ExpenseEntity>>> = emptyList(),
    val total: Double = 0.0
)

class HistoryViewModel(app: ExpenseTrackerApp) : ViewModel() {

    private val repository = ExpenseRepository(app)
    private val _state = MutableStateFlow(HistoryState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allExpenses.collect { expenses ->
                val today = LocalDate.now()
                val groups = mutableListOf<Pair<String, MutableList<ExpenseEntity>>>()
                val map = linkedMapOf<String, MutableList<ExpenseEntity>>()

                for (e in expenses) {
                    val dateStr = e.date.take(10)
                    val label = if (dateStr.isNotBlank()) {
                        val d = try { LocalDate.parse(dateStr) } catch (_: Exception) { null }
                        when {
                            d == null -> "Unknown"
                            d == today -> "Today"
                            d == today.minusDays(1) -> "Yesterday"
                            else -> d.format(DateTimeFormatter.ofPattern("dd MMMM"))
                        }
                    } else "Unknown"

                    val key = dateStr.ifBlank { "unknown" }
                    map.getOrPut(key) { mutableListOf() }.add(e)
                }
                val sorted = map.toList()
                val total = expenses.sumOf { it.amount }
                _state.value = HistoryState(sorted, total)
            }
        }
    }

    class Factory(private val app: ExpenseTrackerApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HistoryViewModel(app) as T
    }
}
