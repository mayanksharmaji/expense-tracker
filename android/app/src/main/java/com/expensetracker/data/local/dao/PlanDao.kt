package com.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.expensetracker.data.local.entity.PlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plan ORDER BY id DESC LIMIT 1")
    fun get(): Flow<PlanEntity?>

    @Query("SELECT * FROM plan ORDER BY id DESC LIMIT 1")
    suspend fun getOnce(): PlanEntity?

    @Insert
    suspend fun insert(plan: PlanEntity): Long

    @Query("DELETE FROM plan")
    suspend fun deleteAll()

    @Query("SELECT * FROM plan WHERE synced = 0")
    suspend fun getUnsynced(): List<PlanEntity>

    @Query("UPDATE plan SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)
}
