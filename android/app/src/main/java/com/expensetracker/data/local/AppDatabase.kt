package com.expensetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.expensetracker.data.local.dao.ExpenseDao
import com.expensetracker.data.local.dao.PlanDao
import com.expensetracker.data.local.entity.ExpenseEntity
import com.expensetracker.data.local.entity.PlanEntity

@Database(entities = [ExpenseEntity::class, PlanEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun planDao(): PlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
