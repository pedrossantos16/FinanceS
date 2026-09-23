package com.pedro.finances.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
    
    // Dialog to name a new table
    var showCreateTableDialog by remember { mutableStateOf(false) }
    var newTableName by remember { mutableStateOf("") }

    // Dialog to add a row to a specific table
    var showAddRowDialogForTable by remember { mutableStateOf<String?>(null) }
    var newCategory by remember { mutableStateOf("") }
    var newTarget by remember { mutableStateOf("") }
    var newTolerance by remember { mutableStateOf("") }

    // Edit cell dialog
    var editingGoal by remember { mutableStateOf<GoalModel?>(null) }
    var editField by remember { mutableStateOf("") } // "category", "target", "tolerance"
    var editValue by remember { mutableStateOf("") }

    // Selected row for delete/duplicate
    var selectedGoalId by remember { mutableStateOf<Long?>(null) }

    // Group goals by table name
    val groupedGoals = goals.groupBy { hb -> hb.tableName }

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

        // Button to create a new meta table
        Button(
            onClick = { showCreateTableDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "+ Criar meta", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedGoals.forEach { (tableName, tableGoals) ->
                item(key = tableName) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Table Title Header with delete table option
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TABELA - $tableName",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                IconButton(onClick = {
                                    dbHelper.deleteTable(tableName)
                                    goals = dbHelper.getGoals()
                                }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Deletar Tabela", tint = Color.Red, modifier = Modifier.size(20.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Table Fields Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF2C2C2C))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Categoria", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), fontSize = 12.sp)
                                Text("Meta", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 12.sp)
                                Text("Tolerância", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Rows
                            tableGoals.forEach { goal ->
                                val isSelected = selectedGoalId == goal.id

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF333333) else Color(0xFF252525)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .combinedClickable(
                                            onClick = { selectedGoalId = if (isSelected) null else goal.id },
                                            onLongClick = { selectedGoalId = goal.id }
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Category Cell
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

                                        if (isSelected) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                IconButton(
                                                    onClick = {
                                                        dbHelper.deleteGoal(goal.id)
                                                        goals = dbHelper.getGoals()
                                                        selectedGoalId = null
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Deletar", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = {
                                                        dbHelper.addGoal(tableName, "${goal.category}_CÓPIA", goal.targetAmount, goal.tolerance)
                                                        goals = dbHelper.getGoals()
                                                        selectedGoalId = null
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicar", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // "+" button at the bottom of the table to add a new row
                            Button(
                                onClick = { showAddRowDialogForTable = tableName },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Linha", tint = Color(0xFFFFD700))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Adicionar linha em $tableName", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog to Create a New Table
    if (showCreateTableDialog) {
        AlertDialog(
            onDismissRequest = { showCreateTableDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Nomear Nova Tabela de Metas", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTableName,
                    onValueChange = { newTableName = it },
                    singleLine = true,
                    label = { Text("Nome da Tabela (ex: LAZER)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFFFD700)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTableName.isNotBlank()) {
                            dbHelper.addGoal(newTableName.uppercase(), "NOVA CATEGORIA", 0.0, 0.0)
                            goals = dbHelper.getGoals()
                            newTableName = ""
                            showCreateTableDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("Criar Tabela", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTableDialog = false }) {
                    Text("Cancelar", color = Color.LightGray)
                }
            }
        )
    }

    // Dialog to Add a Row to a Specific Table
    if (showAddRowDialogForTable != null) {
        AlertDialog(
            onDismissRequest = { showAddRowDialogForTable = null },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Adicionar Linha em $showAddRowDialogForTable", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("Categoria") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = newTarget,
                        onValueChange = { newTarget = it },
                        label = { Text("Meta (R$)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = newTolerance,
                        onValueChange = { newTolerance = it },
                        label = { Text("Tolerância (R$)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = newTarget.toDoubleOrNull() ?: 0.0
                        val tolerance = newTolerance.toDoubleOrNull() ?: 0.0
                        if (newCategory.isNotBlank()) {
                            dbHelper.addGoal(showAddRowDialogForTable!!, newCategory.uppercase(), target, tolerance)
                            goals = dbHelper.getGoals()
                            newCategory = ""
                            newTarget = ""
                            newTolerance = ""
                            showAddRowDialogForTable = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("Adicionar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRowDialogForTable = null }) {
                    Text("Cancelar", color = Color.LightGray)
                }
            }
        )
    }

    // Edit Cell Dialog
    if (editingGoal != null) {
        AlertDialog(
            onDismissRequest = { editingGoal = null },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Editar ${if (editField == "category") "Categoria" else if (editField == "target") "Meta" else "Tolerância"}", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    singleLine = true,
                    label = { Text("Novo Valor") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val g = editingGoal!!
                        val newCat = if (editField == "category") editValue.uppercase() else g.category
                        val newTarget = if (editField == "target") editValue.toDoubleOrNull() ?: g.targetAmount else g.targetAmount
                        val newTol = if (editField == "tolerance") editValue.toDoubleOrNull() ?: g.tolerance else g.tolerance

                        dbHelper.updateGoal(g.id, g.tableName, newCat, newTarget, newTol)
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
                    Text("Cancelar", color = Color.LightGray)
                }
            }
        )
    }
}
