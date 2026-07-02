package com.expensetracker.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.ui.theme.*

val categoryEmojis = mapOf(
    "Food" to "🍔", "Travel" to "🚌", "Shopping" to "🛍",
    "Bills" to "💡", "Medical" to "💊", "Entertainment" to "🎮",
    "Education" to "📚", "Others" to "📦"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddExpense: () -> Unit,
    onHistory: () -> Unit,
    onPlanSetup: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(
        androidx.compose.ui.platform.LocalContext.current.applicationContext as ExpenseTrackerApp
    ))
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💰 Expense Tracker", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = Green500,
                contentColor = TextPrimary
            ) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            val plan = state.plan

            if (state.cycleEnded) {
                item {
                    Banner(text = "Your cycle has ended! Start a new one.", isWarning = true)
                }
            }

            if (plan != null) {
                item { BalanceCard(state) }
                item { HealthCard(state) }
            }

            if (plan == null) {
                item { WelcomeCard(onPlanSetup) }
            }

            if (state.categoryStats.isNotEmpty()) {
                item {
                    SectionCard(title = "Spending by Category") {
                        val maxVal = state.categoryStats.first().second
                        state.categoryStats.forEach { (cat, amount) ->
                            CategoryRow(cat, amount, maxVal)
                        }
                    }
                }
            }

            if (state.recentExpenses.isNotEmpty()) {
                item {
                    SectionCard(title = "Recent Expenses") {
                        state.recentExpenses.forEach { expense ->
                            ExpenseRow(expense, onDelete = { viewModel.deleteExpense(it) })
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = onHistory,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Full History", color = Purple200)
                        }
                    }
                }
            }

            if (plan != null) {
                item { MiniCards(state) }
                item {
                    Button(
                        onClick = onPlanSetup,
                        colors = ButtonDefaults.buttonColors(containerColor = Purple500),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start New Cycle")
                    }
                }
                item {
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Delete Plan?") },
                            text = { Text("Your expenses will be kept. Only the budget plan will be removed.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.deletePlan()
                                    showDeleteDialog = false
                                }) { Text("Delete", color = Red500) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                            }
                        )
                    }
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete Current Plan", color = Red500)
                    }
                }
            }
        }
    }
}

@Composable
fun Banner(text: String, isWarning: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isWarning) Color(0x26FBBF24) else Color(0x2610B981)
    ) {
        Text(
            text = text,
            color = if (isWarning) Yellow500 else Green500,
            modifier = Modifier.padding(14.dp),
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )
    }
}

@Composable
fun BalanceCard(state: DashboardState) {
    val spentPct = if (state.spendingBudget > 0)
        (state.totalSpent / state.spendingBudget * 100).toInt() else 0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(Purple500.copy(alpha = 0.9f), Blue400.copy(alpha = 0.85f))),
                    RoundedCornerShape(24.dp)
                )
                .padding(22.dp)
        ) {
            Column {
                Text("Remaining Budget", fontSize = 11.sp, color = TextPrimary.copy(alpha = 0.75f))
                Spacer(Modifier.height(4.dp))
                Text(
                    "₹${state.remainingBudget.toInt()}",
                    fontSize = 38.sp, fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "of ₹${state.spendingBudget.toInt()}",
                    fontSize = 13.sp, color = TextPrimary.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { spentPct / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TextPrimary,
                    trackColor = TextPrimary.copy(alpha = 0.2f)
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Spent: ₹${state.totalSpent.toInt()}", fontSize = 12.sp, color = TextPrimary.copy(alpha = 0.7f))
                    Text("$spentPct% used", fontSize = 12.sp, color = TextPrimary.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun HealthCard(state: DashboardState) {
    val health = state.health ?: return
    val borderColor = when (health.third) {
        "green" -> Green500
        "yellow" -> Yellow500
        else -> Red500
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCard.copy(alpha = 0.06f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Text(health.first, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(health.second, fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(
                "Today: ₹${state.todaySpent.toInt()} of ₹${state.dailyAllowance}",
                fontSize = 11.sp, color = TextTertiary
            )
        }
    }
}

@Composable
fun WelcomeCard(onPlanSetup: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceCard.copy(alpha = 0.06f)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Set up your plan to start tracking expenses.",
                fontSize = 14.sp, color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onPlanSetup,
                colors = ButtonDefaults.buttonColors(containerColor = Purple500),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start Plan", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceCard.copy(alpha = 0.06f)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 14.dp)
            )
            content()
        }
    }
}

@Composable
fun CategoryRow(category: String, amount: Double, maxVal: Double) {
    val pct = if (maxVal > 0) (amount / maxVal * 100).toInt() else 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(categoryEmojis[category] ?: "📦", fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Text(category, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.width(80.dp))
        Spacer(Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { pct / 100f },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Purple500,
            trackColor = TextPrimary.copy(alpha = 0.08f)
        )
        Spacer(Modifier.width(8.dp))
        Text("₹${amount.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ExpenseRow(expense: com.expensetracker.data.local.entity.ExpenseEntity, onDelete: (Long) -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCard.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(categoryEmojis[expense.category] ?: "📦", fontSize = 22.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("₹${expense.amount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(expense.category, fontSize = 12.sp, color = TextSecondary)
                    if (expense.note.isNotBlank()) {
                        Text(expense.note, fontSize = 11.sp, color = TextTertiary)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (expense.date.length >= 16) {
                    Text(
                        expense.date.substring(11, 16),
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }
                TextButton(
                    onClick = { onDelete(expense.id) },
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text("🗑", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun MiniCards(state: DashboardState) {
    val plan = state.plan ?: return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MiniCard("Pocket Money", "₹${plan.pocketMoney.toInt()}")
        MiniCard("Daily Allowance", "₹${state.dailyAllowance}")
    }
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MiniCard("Savings Goal", "₹${plan.savingsGoal.toInt()}")
        MiniCard("Cycle", "${plan.cycleLength} days")
    }
}

@Composable
fun MiniCard(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCard.copy(alpha = 0.06f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, fontSize = 10.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}
