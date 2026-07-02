package com.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.expensetracker.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllOnce(): List<ExpenseEntity>

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Query("UPDATE expenses SET server_id = :serverId, synced = 1 WHERE id = :localId")
    suspend fun markSynced(localId: Long, serverId: Long)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM expenses WHERE synced = 0")
    suspend fun getUnsynced(): List<ExpenseEntity>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses")
    fun observeTotalSpent(): Flow<Double>
}
