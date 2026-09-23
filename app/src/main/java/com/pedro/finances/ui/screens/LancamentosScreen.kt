package com.pedro.finances.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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

@Composable
fun LancamentosScreen(dbHelper: DatabaseHelper, userCpf: String) {
    var selectedMonth by remember { mutableStateOf("AGOSTO") }
    var selectedYear by remember { mutableStateOf(2026) }
    var expanded by remember { mutableStateOf(false) }

    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") }
    var successMessage by remember { mutableStateOf("") }

    var transactions by remember { mutableStateOf(dbHelper.getTransactions(selectedMonth, userCpf)) }

    LaunchedEffect(selectedMonth, userCpf) {
        transactions = dbHelper.getTransactions(selectedMonth, userCpf)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Title aligned to the left
        Text(
            text = "NOVO LANÇAMENTO",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Month & Year Selector Dropdown
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                onClick = { expanded = true },
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$selectedMonth / $selectedYear",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Selecionar Mês/Ano",
                        tint = Color(0xFFFFD700)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1E1E1E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { selectedYear -= 1 }) {
                        Text("<", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(text = "Ano: $selectedYear", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    TextButton(onClick = { selectedYear += 1 }) {
                        Text(">", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                HorizontalDivider(color = Color.DarkGray)

                val monthsList = listOf(
                    "JANEIRO", "FEVEREIRO", "MARÇO", "ABRIL", "MAIO", "JUNHO",
                    "JULHO", "AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO"
                )
                monthsList.forEach { m ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                m,
                                color = if (selectedMonth == m) Color(0xFFFFD700) else Color.White,
                                fontWeight = if (selectedMonth == m) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            selectedMonth = m
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { type = "EXPENSE" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "EXPENSE") Color(0xFFE53935) else Color(0xFF1E1E1E)
                )
            ) {
                Text("Despesa", color = Color.White)
            }
            Button(
                onClick = { type = "INCOME" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "INCOME") Color(0xFF4CAF50) else Color(0xFF1E1E1E)
                )
            ) {
                Text("Receita", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Categoria (ex: COMBUSTÍVEL)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Valor (R$)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descrição (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            )
        )

        if (successMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = successMessage, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val parsedAmount = amount.toDoubleOrNull()
                if (category.isNotEmpty() && parsedAmount != null) {
                    dbHelper.addTransaction(selectedMonth, category.uppercase(), parsedAmount, type, description, userCpf)
                    successMessage = "Lançamento salvo!"
                    category = ""
                    amount = ""
                    description = ""
                    transactions = dbHelper.getTransactions(selectedMonth, userCpf)
                } else {
                    successMessage = "Preencha categoria e valor válido."
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SALVAR LANÇAMENTO", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Histórico Recente ($selectedMonth)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(transactions) { tx ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = tx.category, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = tx.description, color = Color.Gray, fontSize = 11.sp)
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${if (tx.type == "INCOME") "+" else "-"} R$ %.2f".format(tx.amount),
                                color = if (tx.type == "INCOME") Color(0xFF4CAF50) else Color(0xFFE53935),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            IconButton(
                                onClick = {
                                    dbHelper.deleteTransaction(tx.id, userCpf)
                                    transactions = dbHelper.getTransactions(selectedMonth, userCpf)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
