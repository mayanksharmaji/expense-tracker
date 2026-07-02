package com.expensetracker.data.remote.dto

data class StatsDto(
    val total_spent: Double = 0.0,
    val today_spent: Double = 0.0,
    val spending_budget: Double = 0.0,
    val daily_allowance: Double = 0.0,
    val remaining_budget: Double = 0.0,
    val pocket_money: Double = 0.0,
    val savings_goal: Double = 0.0,
    val cycle_length: Int = 0,
    val days_elapsed: Int = 0,
    val cycle_ended: Boolean = false,
    val expense_count: Int = 0,
    val categories: Map<String, Double> = emptyMap()
)
