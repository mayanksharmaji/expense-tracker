package com.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan")
data class PlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pocketMoney: Double,
    val savingsGoal: Double,
    val cycleLength: Int,
    val startDate: String = "",
    val synced: Boolean = false
)
