package com.expensetracker

import android.app.Application
import com.expensetracker.data.local.AppDatabase
import com.expensetracker.data.sync.SyncManager

class ExpenseTrackerApp : Application() {
    val database by lazy { data.local.AppDatabase.getInstance(this) }
    val syncManager by lazy { SyncManager(this) }

    override fun onCreate() {
        super.onCreate()
        syncManager.startObserving()
    }
}
