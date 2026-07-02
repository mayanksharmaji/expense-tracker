package com.expensetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val category: String,
    val note: String = "",
    val date: String = "",
    @ColumnInfo(name = "server_id") val serverId: Long? = null,
    @ColumnInfo(name = "synced") val synced: Boolean = false
)
