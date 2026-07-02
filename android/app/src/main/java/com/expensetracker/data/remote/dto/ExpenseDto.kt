package com.expensetracker.data.remote.dto

data class ExpenseDto(
    val id: Long? = null,
    val amount: Double,
    val category: String,
    val note: String? = "",
    val date: String? = ""
)
