package com.expensetracker.ui.plan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    onBack: () -> Unit,
    viewModel: PlanViewModel = viewModel(factory = PlanViewModel.Factory(
        androidx.compose.ui.platform.LocalContext.applicationContext as ExpenseTrackerApp
    ))
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isNewCycle) "New Cycle" else "Set Up Plan",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("←", color = Purple200, fontSize = 20.sp) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Text(
                "Welcome!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Set up your plan to start tracking expenses.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
            )

            OutlinedTextField(
                value = state.pocketMoney,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) viewModel.updatePocketMoney(it) },
                label = { Text("Pocket Money (₹)") },
                leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.savingsGoal,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) viewModel.updateSavingsGoal(it) },
                label = { Text("Savings Goal (₹)") },
                leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.cycleLength,
                onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.updateCycleLength(it) },
                label = { Text("Cycle Length (Days)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors()
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = { viewModel.save() },
                enabled = state.pocketMoney.toDoubleOrNull() != null &&
                        state.savingsGoal.toDoubleOrNull() != null &&
                        state.cycleLength.toIntOrNull() != null &&
                        !state.saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple500)
            ) {
                if (state.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (state.isNewCycle) "Start New Cycle" else "Start Plan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (!state.isNewCycle) {
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text("View Expense History")
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = Purple500,
    focusedBorderColor = Purple500.copy(alpha = 0.5f),
    unfocusedBorderColor = TextPrimary.copy(alpha = 0.12f),
    focusedLabelColor = Purple200,
    unfocusedLabelColor = TextSecondary
)
