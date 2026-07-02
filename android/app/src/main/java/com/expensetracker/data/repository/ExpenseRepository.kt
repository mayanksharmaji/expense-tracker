package com.expensetracker.data.repository

import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.local.entity.ExpenseEntity
import com.expensetracker.data.local.entity.PlanEntity
import com.expensetracker.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val app: ExpenseTrackerApp) {

    private val expenseDao = app.database.expenseDao()
    private val planDao = app.database.planDao()
    private val api = RetrofitClient.apiService

    // ── Expenses ──

    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAll()

    suspend fun addExpenseLocally(amount: Double, category: String, note: String): Long {
        return expenseDao.insert(
            ExpenseEntity(
                amount = amount,
                category = category,
                note = note,
                date = "",
                synced = false
            )
        )
    }

    suspend fun syncExpenses() {
        val unsynced = expenseDao.getUnsynced()
        for (local in unsynced) {
            try {
                val res = api.addExpense(
                    mapOf(
                        "amount" to local.amount,
                        "category" to local.category,
                        "note" to local.note
                    )
                )
                if (res.status == "ok") {
                    expenseDao.markSynced(local.id, res.id ?: 0)
                }
            } catch (_: Exception) { }
        }
    }

    suspend fun deleteExpense(id: Long) {
        expenseDao.delete(id)
    }

    suspend fun deleteExpenseAndSync(id: Long) {
        expenseDao.delete(id)
        try { api.deleteExpense(id) } catch (_: Exception) { }
    }

    suspend fun refreshFromServer() {
        try {
            val remote = api.getExpenses()
            for (r in remote) {
                val exists = expenseDao.getAllOnce()
                if (exists.none { it.serverId == r.id }) {
                    expenseDao.insert(
                        ExpenseEntity(
                            amount = r.amount,
                            category = r.category,
                            note = r.note ?: "",
                            date = r.date ?: "",
                            serverId = r.id,
                            synced = true
                        )
                    )
                }
            }
        } catch (_: Exception) { }
    }

    // ── Plan ──

    val plan: Flow<PlanEntity?> = planDao.get()

    suspend fun savePlanLocally(pocketMoney: Double, savingsGoal: Double, cycleLength: Int) {
        planDao.deleteAll()
        planDao.insert(
            PlanEntity(
                pocketMoney = pocketMoney,
                savingsGoal = savingsGoal,
                cycleLength = cycleLength,
                synced = false
            )
        )
    }

    suspend fun syncPlan() {
        val unsynced = planDao.getUnsynced()
        for (p in unsynced) {
            try {
                api.savePlan(
                    mapOf(
                        "pocket_money" to p.pocketMoney,
                        "savings_goal" to p.savingsGoal,
                        "cycle_length" to p.cycleLength
                    )
                )
                planDao.markSynced(p.id)
            } catch (_: Exception) { }
        }
    }

    suspend fun deletePlanAndSync() {
        planDao.deleteAll()
        try { api.deletePlan() } catch (_: Exception) { }
    }

    // ── Sync all ──

    suspend fun syncAll() {
        syncExpenses()
        syncPlan()
    }
}
