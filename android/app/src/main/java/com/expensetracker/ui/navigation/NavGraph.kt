package com.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.expensetracker.ui.add_expense.AddExpenseScreen
import com.expensetracker.ui.dashboard.DashboardScreen
import com.expensetracker.ui.history.HistoryScreen
import com.expensetracker.ui.plan.PlanScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(
                onAddExpense = { navController.navigate("add_expense") },
                onHistory = { navController.navigate("history") },
                onPlanSetup = { navController.navigate("plan") }
            )
        }
        composable("add_expense") {
            AddExpenseScreen(onBack = { navController.popBackStack() })
        }
        composable("history") {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
        composable("plan") {
            PlanScreen(onBack = { navController.popBackStack() })
        }
    }
}
