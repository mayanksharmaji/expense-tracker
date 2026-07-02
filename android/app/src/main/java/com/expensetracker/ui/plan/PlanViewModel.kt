package com.expensetracker.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlanState(
    val pocketMoney: String = "",
    val savingsGoal: String = "",
    val cycleLength: String = "",
    val saving: Boolean = false,
    val saved: Boolean = false,
    val isNewCycle: Boolean = false
)

class PlanViewModel(app: ExpenseTrackerApp) : ViewModel() {

    private val repository = ExpenseRepository(app)
    private val _state = MutableStateFlow(PlanState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val existing = repository.plan
            existing.collect { plan ->
                if (plan != null) {
                    _state.value = PlanState(
                        pocketMoney = plan.pocketMoney.toInt().toString(),
                        savingsGoal = plan.savingsGoal.toInt().toString(),
                        cycleLength = plan.cycleLength.toString(),
                        isNewCycle = true
                    )
                }
            }
        }
    }

    fun updatePocketMoney(v: String) { _state.value = _state.value.copy(pocketMoney = v) }
    fun updateSavingsGoal(v: String) { _state.value = _state.value.copy(savingsGoal = v) }
    fun updateCycleLength(v: String) { _state.value = _state.value.copy(cycleLength = v) }

    fun save() {
        val s = _state.value
        val pm = s.pocketMoney.toDoubleOrNull() ?: return
        val sg = s.savingsGoal.toDoubleOrNull() ?: return
        val cl = s.cycleLength.toIntOrNull() ?: return
        viewModelScope.launch {
            _state.value = s.copy(saving = true)
            if (s.isNewCycle) repository.deletePlanAndSync()
            repository.savePlanLocally(pm, sg, cl)
            repository.syncPlan()
            _state.value = _state.value.copy(saving = false, saved = true)
        }
    }

    class Factory(private val app: ExpenseTrackerApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PlanViewModel(app) as T
    }
}
