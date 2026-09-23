package com.pedro.finances.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.finances.data.DatabaseHelper
import com.pedro.finances.data.GoalModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MetasScreen(dbHelper: DatabaseHelper) {
    var goals by remember { mutableStateOf(dbHelper.getGoals()) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Edit dialog state
    var editingGoal by remember { mutableStateOf<GoalModel?>(null) }
    var editField by remember { mutableStateOf("") } // "category", "target", "tolerance"
    var editValue by remember { mutableStateOf("") }

    // Selected row for delete/duplicate
    var selectedGoalId by remember { mutableStateOf<Long?>(null) }

    var newCategory by remember { mutableStateOf("") }
    var newTarget by remember { mutableStateOf("") }
    var newTolerance by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "Metas",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "+ Criar meta", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            if (selectedGoalId != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = {
                        dbHelper.deleteGoal(selectedGoalId!!)
                        goals = dbHelper.getGoals()
                        selectedGoalId = null
                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Deletar", tint = Color.Red)
                    }
                    IconButton(onClick = {
                        val goalToDup = goals.find { it.id == selectedGoalId }
                        if (goalToDup != null) {
                            dbHelper.addGoal("${goalToDup.category}_CÓPIA", goalToDup.targetAmount, goalToDup.tolerance)
                            goals = dbHelper.getGoals()
                            selectedGoalId = null
                        }
                    }) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicar", tint = Color(0xFFFFD700))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2C2C2C))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Categoria", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), fontSize = 13.sp)
            Text("Meta", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 13.sp)
            Text("Tolerância", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(goals) { goal ->
                val isSelected = selectedGoalId == goal.id

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF333333) else Color(0xFF1E1E1E)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { selectedGoalId = if (isSelected) null else goal.id },
                            onLongClick = { selectedGoalId = goal.id }
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Cell (Click to open edit dialog)
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .combinedClickable(
                                    onClick = {
                                        editingGoal = goal
                                        editField = "category"
                                        editValue = goal.category
                                    }
                                )
                                .padding(4.dp)
                        ) {
                            Text(text = goal.category, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        // Target Cell
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .combinedClickable(
                                    onClick = {
                                        editingGoal = goal
                                        editField = "target"
                                        editValue = goal.targetAmount.toString()
                                    }
                                )
                                .padding(4.dp)
                        ) {
                            Text(text = "R$ %.2f".format(goal.targetAmount), color = Color.Gray, fontSize = 13.sp)
                        }

                        // Tolerance Cell
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .combinedClickable(
                                    onClick = {
                                        editingGoal = goal
                                        editField = "tolerance"
                                        editValue = goal.tolerance.toString()
                                    }
                                )
                                .padding(4.dp)
                        ) {
                            Text(text = "R$ %.2f".format(goal.tolerance), color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // Edit Field Dialog
    if (editingGoal != null) {
        AlertDialog(
            onDismissRequest = { editingGoal = null },
            title = { Text("Editar ${if (editField == "category") "Categoria" else if (editField == "target") "Meta" else "Tolerância"}") },
            text = {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    singleLine = true,
                    label = { Text("Novo Valor") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val g = editingGoal!!
                        val newCat = if (editField == "category") editValue.uppercase() else g.category
                        val newTarget = if (editField == "target") editValue.toDoubleOrNull() ?: g.targetAmount else g.targetAmount
                        val newTol = if (editField == "tolerance") editValue.toDoubleOrNull() ?: g.tolerance else g.tolerance

                        dbHelper.updateGoal(g.id, newCat, newTarget, newTol)
                        goals = dbHelper.getGoals()
                        editingGoal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("Salvar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingGoal = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Add Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Criar Nova Meta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("Categoria") }
                    )
                    OutlinedTextField(
                        value = newTarget,
                        onValueChange = { newTarget = it },
                        label = { Text("Meta (R$)") }
                    )
                    OutlinedTextField(
                        value = newTolerance,
                        onValueChange = { newTolerance = it },
                        label = { Text("Tolerância (R$)") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = newTarget.toDoubleOrNull() ?: 0.0
                        val tolerance = newTolerance.toDoubleOrNull() ?: 0.0
                        if (newCategory.isNotBlank()) {
                            dbHelper.addGoal(newCategory.uppercase(), target, tolerance)
                            goals = dbHelper.getGoals()
                            newCategory = ""
                            newTarget = ""
                            newTolerance = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("Salvar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
