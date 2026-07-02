package com.expensetracker.ui.add_expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class AddExpenseState(
    val amount: String = "",
    val category: String = "Food",
    val note: String = "",
    val saving: Boolean = false,
    val saved: Boolean = false
)

class AddExpenseViewModel(app: ExpenseTrackerApp) : ViewModel() {

    private val repository = ExpenseRepository(app)
    private val _state = MutableStateFlow(AddExpenseState())
    val state = _state.asStateFlow()

    fun updateAmount(v: String) { _state.value = _state.value.copy(amount = v) }
    fun updateCategory(v: String) { _state.value = _state.value.copy(category = v) }
    fun updateNote(v: String) { _state.value = _state.value.copy(note = v) }

    fun save() {
        val s = _state.value
        val amt = s.amount.toDoubleOrNull() ?: return
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        viewModelScope.launch {
            _state.value = s.copy(saving = true)
            repository.addExpenseLocally(amt, s.category, s.note, now)
            repository.syncExpenses()
            _state.value = _state.value.copy(saving = false, saved = true)
        }
    }

    class Factory(private val app: ExpenseTrackerApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AddExpenseViewModel(app) as T
    }
}
